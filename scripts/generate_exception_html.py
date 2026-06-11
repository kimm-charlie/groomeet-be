#!/usr/bin/env python3
"""
Exception Codes HTML Generator
===============================

이 스크립트는 exception-codes.md 파일을 파싱하여 HTML 문서를 자동 생성합니다.

실행 방법:
---------
1. 수동 실행 (프로젝트 루트에서):
   $ python3 scripts/generate_exception_html.py

2. Gradle build 실행 (자동):
   $ ./gradlew clean build

   → generateExceptionDocs task가 자동으로 실행됩니다

3. 실행 조건:
   - Python 3.6 이상 필요
   - exception-codes.md 파일이 docs/ 에 존재해야 함

4. 동작 방식:
   - markdown 파일에서 exception 정보를 파싱
   - HTML을 build/docs/exception-codes.html에 생성
   - copyDocs task가 static/docs/로 복사

사용 시나리오:
-------------
- 새로운 Exception 클래스 추가 후 문서 업데이트할 때
- exception-codes.md 파일을 수정한 후 HTML 문서 재생성할 때

주의사항:
--------
- 매번 새로 생성됩니다 (이전 HTML은 덮어씁니다)
"""

import re
from pathlib import Path
from datetime import datetime

# Complete Asciidoctor CSS (from REST Docs)
ASCIIDOCTOR_CSS = """/* Asciidoctor default stylesheet | MIT License | https://asciidoctor.org */
article,aside,details,figcaption,figure,footer,header,hgroup,main,nav,section{display:block}
audio,video{display:inline-block}
audio:not([controls]){display:none;height:0}
html{font-family:sans-serif;-ms-text-size-adjust:100%;-webkit-text-size-adjust:100%}
a{background:none}
a:focus{outline:thin dotted}
a:active,a:hover{outline:0}
h1{font-size:2em;margin:.67em 0}
abbr[title]{border-bottom:1px dotted}
b,strong{font-weight:bold}
dfn{font-style:italic}
hr{-moz-box-sizing:content-box;box-sizing:content-box;height:0}
mark{background:#ff0;color:#000}
code,kbd,pre,samp{font-family:monospace;font-size:1em}
pre{white-space:pre-wrap}
q{quotes:"\\201C" "\\201D" "\\2018" "\\2019"}
small{font-size:80%}
sub,sup{font-size:75%;line-height:0;position:relative;vertical-align:baseline}
sup{top:-.5em}
sub{bottom:-.25em}
img{border:0}
svg:not(:root){overflow:hidden}
figure{margin:0}
fieldset{border:1px solid silver;margin:0 2px;padding:.35em .625em .75em}
legend{border:0;padding:0}
button,input,select,textarea{font-family:inherit;font-size:100%;margin:0}
button,input{line-height:normal}
button,select{text-transform:none}
button,html input[type="button"],input[type="reset"],input[type="submit"]{-webkit-appearance:button;cursor:pointer}
button[disabled],html input[disabled]{cursor:default}
input[type="checkbox"],input[type="radio"]{box-sizing:border-box;padding:0}
button::-moz-focus-inner,input::-moz-focus-inner{border:0;padding:0}
textarea{overflow:auto;vertical-align:top}
table{border-collapse:collapse;border-spacing:0}
*,*::before,*::after{-moz-box-sizing:border-box;-webkit-box-sizing:border-box;box-sizing:border-box}
html,body{font-size:100%}
body{background:#fff;color:rgba(0,0,0,.8);padding:0;margin:0;font-family:"Noto Serif","DejaVu Serif",serif;font-weight:400;font-style:normal;line-height:1;position:relative;cursor:auto;tab-size:4;-moz-osx-font-smoothing:grayscale;-webkit-font-smoothing:antialiased}
a:hover{cursor:pointer}
img,object,embed{max-width:100%;height:auto}
object,embed{height:100%}
img{-ms-interpolation-mode:bicubic}
.left{float:left!important}
.right{float:right!important}
.text-left{text-align:left!important}
.text-right{text-align:right!important}
.text-center{text-align:center!important}
.text-justify{text-align:justify!important}
.hide{display:none}
img,object,svg{display:inline-block;vertical-align:middle}
textarea{height:auto;min-height:50px}
select{width:100%}
.center{margin-left:auto;margin-right:auto}
.stretch{width:100%}
div,dl,dt,dd,ul,ol,li,h1,h2,h3,#toctitle,h4,h5,h6,pre,form,p,blockquote,th,td{margin:0;padding:0;direction:ltr}
a{color:#2156a5;text-decoration:underline;line-height:inherit}
a:hover,a:focus{color:#1d4b8f}
a img{border:0}
p{font-family:inherit;font-weight:400;font-size:1em;line-height:1.6;margin-bottom:1.25em;text-rendering:optimizeLegibility}
h1,h2,h3,#toctitle,h4,h5,h6{font-family:"Open Sans","DejaVu Sans",sans-serif;font-weight:300;font-style:normal;color:#ba3925;text-rendering:optimizeLegibility;margin-top:1em;margin-bottom:.5em;line-height:1.0125em}
h1{font-size:2.125em}
h2{font-size:1.6875em}
h3,#toctitle{font-size:1.375em}
h4,h5{font-size:1.125em}
h6{font-size:1em}
hr{border:solid #dddddf;border-width:1px 0 0;clear:both;margin:1.25em 0 1.1875em;height:0}
code{font-family:"Droid Sans Mono","DejaVu Sans Mono",monospace;font-weight:400;color:rgba(0,0,0,.9)}
@media screen and (min-width:768px){h1{font-size:2.75em}
h2{font-size:2.3125em}
h3,#toctitle{font-size:1.6875em}
h4{font-size:1.4375em}}
table{background:#fff;margin-bottom:1.25em;border:solid 1px #dedede}
table thead,table tfoot{background:#f7f8f7}
table thead tr th,table thead tr td,table tfoot tr th,table tfoot tr td{padding:.5em .625em .625em;font-size:inherit;color:rgba(0,0,0,.8);text-align:left}
table tr th,table tr td{padding:.5625em .625em;font-size:inherit;color:rgba(0,0,0,.8)}
table tr.even,table tr.alt{background:#f8f8f7}
:not(pre):not([class^=L])>code{font-size:.9375em;font-style:normal!important;letter-spacing:0;padding:.1em .5ex;word-spacing:-.15em;background:#f7f7f8;-webkit-border-radius:4px;border-radius:4px;line-height:1.45;text-rendering:optimizeSpeed;word-wrap:break-word}
#header,#content,#footnotes,#footer{width:100%;margin-left:auto;margin-right:auto;margin-top:0;margin-bottom:0;max-width:62.5em;*zoom:1;position:relative;padding-left:.9375em;padding-right:.9375em}
#content{margin-top:1.25em}
#header>h1:first-child{color:rgba(0,0,0,.85);margin-top:2.25rem;margin-bottom:0}
#header>h1:first-child+#toc{margin-top:8px;border-top:1px solid #dddddf}
#header>h1:only-child,body.toc2 #header>h1:nth-last-child(2){border-bottom:1px solid #dddddf;padding-bottom:8px}
#header .details{border-bottom:1px solid #dddddf;line-height:1.45;padding-top:.25em;padding-bottom:.25em;padding-left:.25em;color:rgba(0,0,0,.6);display:flex;flex-flow:row wrap}
#toc{border-bottom:1px solid #e7e7e9;padding-bottom:.5em}
#toc>ul{margin-left:.125em}
#toc ul{font-family:"Open Sans","DejaVu Sans",sans-serif;list-style-type:none}
#toc li{line-height:1.3334;margin-top:.3334em}
#toc a{text-decoration:none}
#toc a:active{text-decoration:underline}
#toctitle{color:#7a2518;font-size:1.2em}
@media screen and (min-width:768px){#toctitle{font-size:1.375em}
body.toc2{padding-left:15em;padding-right:0}
#toc.toc2{margin-top:0!important;background:#f8f8f7;position:fixed;width:15em;left:0;top:0;border-right:1px solid #e7e7e9;border-top-width:0!important;border-bottom-width:0!important;z-index:1000;padding:1.25em 1em;height:100%;overflow:auto}
#toc.toc2 #toctitle{margin-top:0;margin-bottom:.8rem;font-size:1.2em}
#toc.toc2>ul{font-size:.9em;margin-bottom:0}
#toc.toc2 ul ul{margin-left:0;padding-left:1em}}
@media screen and (min-width:1280px){body.toc2{padding-left:20em;padding-right:0}
#toc.toc2{width:20em}
#toc.toc2 #toctitle{font-size:1.375em}
#toc.toc2>ul{font-size:.95em}
#toc.toc2 ul ul{padding-left:1.25em}}
#footer{max-width:100%;background:rgba(0,0,0,.8);padding:1.25em}
#footer-text{color:rgba(255,255,255,.8);line-height:1.44}
.sect1{padding-bottom:.625em}
@media screen and (min-width:768px){.sect1{padding-bottom:1.25em}}
.sect1:last-child{padding-bottom:0}
.sect1+.sect1{border-top:1px solid #e7e7e9}
h2>a.anchor{position:absolute;z-index:1001;width:1.5ex;margin-left:-1.5ex;display:block;text-decoration:none!important;visibility:hidden;text-align:center;font-weight:400}
h2>a.anchor::before{content:"\\00A7";font-size:.85em;display:block;padding-top:.1em}
h2:hover>a.anchor,h2>a.anchor:hover{visibility:visible}
h2>a.link{color:#ba3925;text-decoration:none}
h2>a.link:hover{color:#a53221}
table.tableblock{max-width:100%;border-collapse:separate}
table.tableblock,th.tableblock,td.tableblock{border:0 solid #dedede}
table.grid-all>thead>tr>.tableblock,table.grid-all>tbody>tr>.tableblock{border-width:0 1px 1px 0}
table.grid-all>tfoot>tr>.tableblock{border-width:1px 1px 0 0}
table.grid-all>*>tr>.tableblock:last-child{border-right-width:0}
table.grid-all>tbody>tr:last-child>.tableblock,table.grid-all>thead:last-child>tr>.tableblock{border-bottom-width:0}
table.frame-all{border-width:1px}
th.halign-left,td.halign-left{text-align:left}
th.halign-right,td.halign-right{text-align:right}
th.halign-center,td.halign-center{text-align:center}
th.valign-top,td.valign-top{vertical-align:top}
th.valign-bottom,td.valign-bottom{vertical-align:bottom}
th.valign-middle,td.valign-middle{vertical-align:middle}
table thead th,table tfoot th{font-weight:bold}
p.tableblock{font-size:1em}
"""

def parse_markdown_exceptions(md_content):
    """Parse markdown and extract all exception information with 5 columns."""
    exceptions = []
    current_exception = None

    lines = md_content.split('\n')
    i = 0

    while i < len(lines):
        line = lines[i].strip()

        # Match exception header (## ExceptionName)
        if line.startswith('## '):
            if current_exception:
                exceptions.append(current_exception)

            exception_name = line[3:].strip()
            current_exception = {
                'name': exception_name,
                'source_file': None,
                'errors': []
            }

        # Match source file
        elif line.startswith('Source file:') and current_exception:
            match = re.search(r'`([^`]+)`', line)
            if match:
                current_exception['source_file'] = match.group(1)

        # Match table rows (Code | HTTP Status | Name | Message | Description)
        elif line.startswith('|') and current_exception:
            # Skip header and separator rows
            if 'Code' in line or '---' in line:
                i += 1
                continue

            # Parse table row: | Code | HTTP Status | Name | Message | Description |
            parts = [p.strip() for p in line.split('|')]
            if len(parts) >= 6:  # Empty + 5 columns + Empty
                code = parts[1]
                status = parts[2]
                name = parts[3]
                message = parts[4]
                description = parts[5]

                if code and status:  # Valid row
                    current_exception['errors'].append({
                        'code': code,
                        'status': status,
                        'name': name,
                        'message': message,
                        'description': description
                    })

        i += 1

    # Add last exception
    if current_exception:
        exceptions.append(current_exception)

    return exceptions

def generate_html_section(exception):
    """Generate HTML section for a single exception."""
    name = exception['name']
    source_file = exception['source_file'] or 'Unknown'
    errors = exception['errors']

    html = f'''<div class="sect1">
<h2 id="{name}"><a class="anchor" href="#{name}"></a><a class="link" href="#{name}">{name}</a></h2>
<div class="sectionbody">
<div class="paragraph">
<p>Source file: <code>{source_file}</code></p>
</div>
<table class="tableblock frame-all grid-all stretch">
<colgroup>
<col style="width: 15%;">
<col style="width: 20%;">
<col style="width: 20%;">
<col style="width: 25%;">
<col style="width: 20%;">
</colgroup>
<thead>
<tr>
<th class="tableblock halign-left valign-top">Code</th>
<th class="tableblock halign-left valign-top">HTTP Status</th>
<th class="tableblock halign-left valign-top">Name</th>
<th class="tableblock halign-left valign-top">Message</th>
<th class="tableblock halign-left valign-top">Description</th>
</tr>
</thead>
<tbody>
'''

    for error in errors:
        html += f'''<tr>
<td class="tableblock halign-left valign-top"><p class="tableblock"><code>{error['code']}</code></p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">{error['status']}</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">{error['name']}</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">{error['message']}</p></td>
<td class="tableblock halign-left valign-top"><p class="tableblock">{error['description']}</p></td>
</tr>
'''

    html += '''</tbody>
</table>
</div>
</div>
'''

    return html

def generate_complete_html(exceptions, generated_date):
    """Generate complete HTML document with all exceptions."""

    # Generate TOC entries
    toc_entries = '\n'.join([f'<li><a href="#{e["name"]}">{e["name"]}</a></li>' for e in exceptions])

    # Generate all exception sections
    sections = '\n'.join([generate_html_section(e) for e in exceptions])

    html = f'''<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="generator" content="Asciidoctor 2.0.10">
<title>Exception Codes</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Open+Sans:300,300italic,400,400italic,600,600italic%7CNoto+Serif:400,400italic,700,700italic%7CDroid+Sans+Mono:400,700">
<style>
{ASCIIDOCTOR_CSS}
</style>
</head>
<body class="book toc2 toc-left">
<div id="header">
<h1>Exception Codes</h1>
<div class="details">
<span id="revnumber">version 0.0.1-SNAPSHOT</span>
</div>
<div id="toc" class="toc2">
<div id="toctitle">목차</div>
<ul class="sectlevel1">
{toc_entries}
</ul>
</div>
</div>
<div id="content">
<div id="preamble">
<div class="sectionbody">
<div class="paragraph">
<p>Source: <code>src/main/java/com/motd/be/exception/exceptions</code><br>
Generated: {generated_date}</p>
</div>
</div>
</div>
{sections}
</div>
<div id="footer">
<div id="footer-text">
Version 0.0.1-SNAPSHOT<br>
Last updated {generated_date}
</div>
</div>
</body>
</html>'''

    return html

def main():
    # File paths (relative to script location)
    script_dir = Path(__file__).parent.resolve()  # scripts/
    project_root = script_dir.parent  # project root

    md_file = project_root / 'docs' / 'exception-codes.md'
    html_file = project_root / 'build' / 'docs' / 'exception-codes.html'

    # Ensure build/docs directory exists
    html_file.parent.mkdir(parents=True, exist_ok=True)

    # Read markdown file
    print(f"Reading markdown file: {md_file}")
    if not md_file.exists():
        print(f"ERROR: Markdown file not found: {md_file}")
        return
    md_content = md_file.read_text(encoding='utf-8')

    # Parse exceptions
    print("Parsing exceptions from markdown...")
    exceptions = parse_markdown_exceptions(md_content)
    print(f"Found {len(exceptions)} exceptions")

    # Get current date
    current_date = datetime.now().strftime('%Y-%m-%d')

    # Generate complete HTML
    print("Generating HTML...")
    final_html = generate_complete_html(exceptions, current_date)

    # Write HTML file
    print(f"Writing HTML to: {html_file}")
    html_file.write_text(final_html, encoding='utf-8')
    print(f"Done! Generated {len(exceptions)} exception sections.")

if __name__ == '__main__':
    main()

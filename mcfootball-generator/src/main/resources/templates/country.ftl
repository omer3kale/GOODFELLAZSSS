<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${countryName} — GOODFELLAZßS</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; }
    body { margin: 0; padding: 0; background-color: #ffffff; color: #000000;
           font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif; }
    header, footer { background-color: #000000; color: #ffffff; padding: 1rem 2rem; }
    .brand { font-weight: bold; color: #e60000; letter-spacing: 0.08em;
             text-transform: uppercase; font-size: 1.4rem; }
    .brand a { color: #e60000; text-decoration: none; }
    .nav { margin-top: 0.5rem; }
    .nav a { color: #ffffff; text-decoration: none; margin-right: 1rem; font-size: 0.95rem; }
    .nav a:hover { text-decoration: underline; }
    .content { max-width: 1100px; margin: 0 auto; padding: 1.5rem 1rem 3rem; }
    header .content, footer .content { padding: 1rem 0; }
    a { color: #1e90ff; }
    h1 { color: #000000; border-bottom: 3px solid #e60000; padding-bottom: 0.5rem; display: inline-block; }
    h2 { color: #333333; }
    .league-list { list-style: none; padding: 0; }
    .league-list li { padding: 0.6rem 0; border-bottom: 1px solid #eeeeee; }
    .league-list li:last-child { border-bottom: none; }
    footer .brand { font-size: 1rem; }
  </style>
</head>
<body>
  <header>
    <div class="content">
      <div class="brand"><a href="../index.html">GOODFELLAZßS</a></div>
      <nav class="nav">
        <a href="../index.html">Home</a>
      <#list countries as c>
        <a href="../${c.slug}/index.html">${c.name}</a>
      </#list>
      </nav>
    </div>
  </header>

  <main>
    <div class="content">
      <h1>${countryName}</h1>

      <h2>Leagues</h2>
      <ul class="league-list">
      <#list leagues as league>
        <li><a href="${league.slug}/index.html">${league.name}</a> — Season ${league.season}</li>
      </#list>
      </ul>
    </div>
  </main>

  <footer>
    <div class="content">
      <span class="brand">GOODFELLAZßS</span>
      &nbsp;– European Football 2025/2026
    </div>
  </footer>
</body>
</html>

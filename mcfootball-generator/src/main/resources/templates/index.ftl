<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>GOODFELLAZßS — Home</title>
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
    .country-card { margin-bottom: 1.5rem; }
    .country-card h3 { margin: 0 0 0.3rem; }
    .country-card ul { margin: 0.2rem 0 0 1.2rem; padding: 0; }
    .country-card li { margin-bottom: 0.2rem; }
    footer .brand { font-size: 1rem; }
  </style>
</head>
<body>
  <header>
    <div class="content">
      <div class="brand">GOODFELLAZßS</div>
      <nav class="nav">
        <a href="index.html">Home</a>
      <#list countries as country>
        <a href="${country.slug}/index.html">${country.name}</a>
      </#list>
      </nav>
    </div>
  </header>

  <main>
    <div class="content">
      <h1>European Football 2025/2026</h1>
      <p>Results across the top three divisions.</p>

      <h2>Countries &amp; Leagues</h2>
    <#list countries as country>
      <div class="country-card">
        <h3><a href="${country.slug}/index.html">${country.name}</a></h3>
        <ul>
        <#list country.leagues as league>
          <li><a href="${country.slug}/${league.slug}/index.html">${league.name}</a> — ${league.season}</li>
        </#list>
        </ul>
      </div>
    </#list>
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

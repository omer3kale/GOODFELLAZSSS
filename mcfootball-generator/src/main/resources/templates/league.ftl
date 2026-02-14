<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${leagueName} — ${countryName} — GOODFELLAZFß</title>
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
    .breadcrumb { color: #333333; font-size: 0.9rem; margin-bottom: 1rem; }
    .breadcrumb a { color: #1e90ff; }
    .team-name { color: #1e90ff; font-weight: 600; }
    .score { color: #e60000; font-weight: 700; }
    .match-meta { color: #333333; font-size: 0.9rem; }
    table { width: 100%; border-collapse: collapse; }
    th { background-color: #000000; color: #ffffff; padding: 0.6rem 0.8rem; text-align: left; font-weight: 600; }
    td { padding: 0.6rem 0.8rem; border-bottom: 1px solid #eeeeee; }
    tr:nth-child(even) { background-color: #fafafa; }
    footer .brand { font-size: 1rem; }
  </style>
</head>
<body>
  <header>
    <div class="content">
      <div class="brand"><a href="../../index.html">GOODFELLAZFß</a></div>
      <nav class="nav">
        <a href="../../index.html">Home</a>
      <#list countries as c>
        <a href="../../${c.slug}/index.html">${c.name}</a>
      </#list>
      </nav>
    </div>
  </header>

  <main>
    <div class="content">
      <div class="breadcrumb">
        <a href="../../index.html">Home</a> &rsaquo;
        <a href="../index.html">${countryName}</a> &rsaquo;
        ${leagueName}
      </div>

      <h1>${leagueName}</h1>
      <h2>Season ${season}</h2>

    <#if (matches?size > 0)>
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Time</th>
            <th>Home</th>
            <th>Away</th>
            <th>Score</th>
            <th>Stadium</th>
          </tr>
        </thead>
        <tbody>
        <#list matches as m>
          <tr>
            <td class="match-meta">${m.date}</td>
            <td class="match-meta">${m.time}</td>
            <td><span class="team-name">${m.homeTeam}</span> <span class="match-meta">(${m.homeCity})</span></td>
            <td><span class="team-name">${m.awayTeam}</span> <span class="match-meta">(${m.awayCity})</span></td>
            <td><span class="score">${m.homeScore} – ${m.awayScore}</span></td>
            <td class="match-meta">${m.stadium}</td>
          </tr>
        </#list>
        </tbody>
      </table>
    <#else>
      <p>No matches recorded yet.</p>
    </#if>
    </div>
  </main>

  <footer>
    <div class="content">
      <span class="brand">GOODFELLAZFß</span>
      &nbsp;– European Football 2025/2026
    </div>
  </footer>
</body>
</html>

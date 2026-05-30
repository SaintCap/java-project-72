@param gg.jte.html.HtmlContent content

<!doctype html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Page Analyzer</title>

    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet"
    >
</head>

<body>

<nav class="navbar navbar-expand-lg navbar-light bg-white border-bottom">
    <div class="container">
        <a class="navbar-brand" href="/">Анализатор страниц</a>
    </div>
</nav>

<main class="container mt-5">
    ${content}
</main>

</body>
</html>
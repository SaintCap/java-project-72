@param gg.jte.html.HtmlContent content
@param String flash = ""

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

<nav class="navbar navbar-expand-lg navbar-dark bg-dark border-bottom">
    <div class="container">
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav">
                <li class="nav-item">
                    <a class="nav-link text-white" href="/">Анализатор страниц</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link text-white" href="/urls">Сайты</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<main class="container mt-5">
    @if(!flash.isEmpty())
        <div class="alert alert-info">
            ${flash}
        </div>
    @endif
    ${content}
</main>

</body>
</html>
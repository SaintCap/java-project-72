@import hexlet.code.model.Url
@import hexlet.code.model.UrlCheck
@import java.util.List
@import java.time.format.DateTimeFormatter

@param Url url
@param List<UrlCheck> checks
@param String flash = null

@template.layout(flash = flash, content = @`

    <h1>${url.getName()}</h1>

    <table class="table" data-test="url">
        <tbody>
        <tr>
            <td>ID</td>
            <td>${url.getId()}</td>
        </tr>

        <tr>
            <td>Имя</td>
            <td>${url.getName()}</td>
        </tr>

        <tr>
            <td>Дата создания</td>
            <td>${url.getCreatedAt().toLocalDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}</td>
        </tr>
        </tbody>
    </table>

    <h2 class="mt-5 mb-3">Проверки</h2>

    <form method="post" action="/urls/${url.getId()}/checks" class="mb-4">
        <button type="submit" class="btn btn-primary">
            Запустить проверку
        </button>
    </form>

    <table class="table table-striped table-hover" data-test="checks">
        <thead class="thead-light" style="border-bottom: 3px solid #0d6efd;">
        <tr>
            <th class="text-muted small text-uppercase">ID</th>
            <th class="text-muted small text-uppercase">Код ответа</th>
            <th class="text-muted small text-uppercase">H1</th>
            <th class="text-muted small text-uppercase">Title</th>
            <th class="text-muted small text-uppercase">Description</th>
            <th class="text-muted small text-uppercase">Дата проверки</th>
        </tr>
        </thead>
        <tbody>
        @for(var check : checks)
            <tr>
                <td class="font-weight-bold">${check.getId()}</td>
                <td>${check.getStatusCode()}</td>
                <td>${check.getShortH1()}</td>
                <td>${check.getShortTitle()}</td>
                <td>${check.getShortDescription()}</td>
                <td class="text-nowrap">
                    ${check.getCreatedAt()
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
                </td>
            </tr>
        @endfor
        </tbody>
    </table>

`)
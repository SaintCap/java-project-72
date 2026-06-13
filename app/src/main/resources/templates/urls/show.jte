@import hexlet.code.model.Url
@import java.time.format.DateTimeFormatter
@param Url url
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

    <table class="table table-striped" data-test="checks">
        <thead>
        <tr>
            <th>ID</th>
            <th>Код ответа</th>
            <th>h1</th>
            <th>title</th>
            <th>description</th>
            <th>Дата создания</th>
        </tr>
        </thead>

        <tbody>
        <!-- Здесь будут строки проверок -->
        </tbody>
    </table>

`)
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

`)

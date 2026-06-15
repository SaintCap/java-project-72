@import hexlet.code.model.Url
@import hexlet.code.model.UrlCheck
@import java.util.List
@import java.util.Map
@import java.time.format.DateTimeFormatter
@param List<Url> urls
@param Map<Long, UrlCheck> latestChecks
@param String flash = ""

@template.layout(flash = flash, content = @`
    <h1>Сайты</h1>

    <table class="table" data-test="urls">

        <thead>
        <tr>
            <th>ID</th>
            <th>Имя</th>
            <th>Дата создания</th>
            <th>Код ответа</th>
        </tr>
        </thead>

        <tbody>

        @for(var url : urls)

            <tr>
                <td>${url.getId()}</td>

                <td>
                    <a href="/urls/${url.getId()}">
                        ${url.getName()}
                    </a>
                </td>

                <td>${url.getCreatedAt().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}</td>

                <td>
                    @if(latestChecks.get(url.getId()) != null)
                        ${latestChecks.get(url.getId()).getStatusCode()}
                    @endif
                </td>
            </tr>

        @endfor

        </tbody>

    </table>
`)

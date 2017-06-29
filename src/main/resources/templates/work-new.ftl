<#import "macros.ftl" as macros>
<@macros.localLayout ctx=ctx >
  <div>
    <form action="/admin/${orcid}/work/create" method="POST">
      <div class="form-group">
        <label>Work type</label>
        <select name="type" class="form-control">
        <#list workTypeValues as workTypeValue>
          <option value="${workTypeValue.value()}"
            <#if (params["type"][0])! == workTypeValue.value()> selected </#if>>
            ${workTypeValue.value()}
          </option>
        </#list>
        </select>
      </div>
      <div class="form-group">
        <label>Title</label>
        <input type="text" class="form-control" name="title" value="${(params["title"][0])!}">
      </div>
      <div class="form-group">
        <label>Journal Title</label>
        <input type="text" class="form-control" name="journal-title" value="${(params["journal-title"][0])!}">
      </div>
      <div class="form-group">
        <label>URL</label>
        <input type="text" class="form-control" name="url" value="${(params["url"][0])!}">
      </div>
      <div class="form-group">
        <button class="btn btn-default">OK</button>
      </div>
    </form>
  </div>
</@macros.localLayout>
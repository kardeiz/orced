<#import "macros.ftl" as macros>
<@macros.localLayout ctx=ctx >
  <div>
    <table class="table table-vc">
      <tr>
          <td>Name</td>
          <td>ORCID</td>
          <td>Expires at</td>
          <td>Action</td>
        </tr>
      <#list tokenRecords as tokenRecord>
        <tr>
          <td>${tokenRecord.name}</td>
          <td>${tokenRecord.orcid}</td>
          <td>${tokenRecord.expiresAt.toString("yyyy-MM-dd")}</td>
          <td><a class="btn btn-primary" href="/admin/${tokenRecord.orcid}/employment/new"><i class="fa fa-plus"></i> Add employment</a></td>
          <td><a class="btn btn-primary" href="/admin/${tokenRecord.orcid}/work/new"><i class="fa fa-plus"></i> Add work</a></td>
        </tr>
      </#list>
    </table>
  </div>
</@macros.localLayout>
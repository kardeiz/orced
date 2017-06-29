<#import "macros.ftl" as macros>
<@macros.localLayout ctx=ctx >
  <div>
    <form action="/admin/${orcid}/employment/create" method="POST">
      <div class="form-group">
        <label>Department Name</label>
        <input type="text" class="form-control" name="department-name" value="${(params["department-name"][0])!}">
      </div>
      <div class="form-group">
        <label>Role Title</label>
        <input type="text" class="form-control" name="role-title" value="${(params["role-title"][0])!}">
      </div>
      <div class="form-group">
        <div class="row">
          <div class="col-md-4">
            <label>Start Date Year</label>
            <input type="text" class="form-control" name="start-date.year.value" value="${(params["start-date.year.value"][0])!}">
          </div>
          <div class="col-md-4">
            <label>Start Date Month</label>
            <input type="text" class="form-control" name="start-date.month.value" value="${(params["start-date.month.value"][0])!}">
          </div>
          <div class="col-md-4">
            <label>Start Date Day</label>
            <input type="text" class="form-control" name="start-date.day.value" value="${(params["start-date.day.value"][0])!}">
          </div>
        </div>      
      </div>
      <div class="form-group">
        <div class="row">
          <div class="col-md-4">
            <label>End Date Year</label>
            <input type="text" class="form-control" name="end-date.year.value" value="${(params["end-date.year.value"][0])!}">
          </div>
          <div class="col-md-4">
            <label>End Date Month</label>
            <input type="text" class="form-control" name="end-date.month.value" value="${(params["end-date.month.value"][0])!}">
          </div>
          <div class="col-md-4">
            <label>End Date Day</label>
            <input type="text" class="form-control" name="end-date.day.value" value="${(params["end-date.day.value"][0])!}">
          </div>
        </div>      
      </div>
      <div class="form-group">
        <label>Organization Name</label>
        <input type="text" class="form-control" name="organization.name" value="${(params["organization.name"][0])!}">
      </div>
      <div class="form-group">
        <label>Organization City</label>
        <input type="text" class="form-control" name="organization.address.city" value="${(params["organization.address.city"][0])!}">
      </div>
      <div class="form-group">
        <label>Organization Region</label>
        <input type="text" class="form-control" name="organization.address.region" value="${(params["organization.address.region"][0])!}">
      </div>
      <div class="form-group">
        <label>Organization Country</label>
        <input type="text" class="form-control" name="organization.address.country" value="${(params["organization.address.country"][0])!}">
      </div>
      <div class="form-group">
        <button class="btn btn-default">OK</button>
      </div>
    </form>
  </div>
</@macros.localLayout>
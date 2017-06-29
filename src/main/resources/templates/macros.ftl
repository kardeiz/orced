<#macro localLayout ctx head="" title="ORCED">
  <html lang="en">
    <head>
      <meta charset="utf-8"/>
      <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
      <meta name="viewport" content="width=device-width, initial-scale=1"/>
      <title>${title}</title>
      <link href="/assets/application.min.css" rel="stylesheet"/>
      <link rel="icon" href="/assets/images/favicon.ico" type="image/x-icon" />
      <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
      <![endif]-->
      ${head!has_content}
    </head>
    <body>
      <div class="container">      
        <div class="header clearfix">
          <nav>
            <ul class="nav nav-pills pull-right">
              <li role="presentation"><a href="/">Home</a></li>
              <li role="presentation"><a href="${ctx.orcidAuthUrlForAuthorize}">Authorize this app</a></li>
              <#if ctx.user??>
                <li role="presentation"><a href="/sign-out">Sign out</a></li>
              <#else>
                <li role="presentation"><a href="${ctx.orcidAuthUrlForSignIn}">Sign in</a></li>
              </#if>
              <li role="presentation"><a href="/admin/token-records">Token records</a></li>
            </ul>
          </nav>
          <h3>TCU ORCID Editor</h3>
        </div>
        <div class="body">
          <#if ctx.flash??>
            <#list ctx.flash.inner as f>
              <div class="alert alert-${f.key}" role="alert">${f.value}</div>
            </#list>
          </#if>
          <div class="content">          
            <#nested/>
          </div>
        </div>
        <div class="footer clearfix">
          <p class="pull-left">&copy; TCU Library ${ctx.thisYear}</p>
        </div>
      </div>
      <script src="/assets/application.min.js"></script>
    </body>
  </html>
</#macro>
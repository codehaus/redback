<%--
  ~ Copyright 2005-2006 The Codehaus.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="ww" uri="/webwork" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="request.password.reset.page.title"/></title>
</head>

<body onload="javascript:document.forms['passwordReset'].username.focus();">

<h2><ww:text name="request.password.reset.section.title"/></h2>

<%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

<ww:form action="passwordReset" namespace="/security" theme="xhtml" 
         id="passwordResetForm" method="post" name="passwordReset" cssClass="security passwordReset">
  <ww:textfield label="%{getText('username')}" name="username" size="30" required="true" />
  <ww:submit value="%{getText('request.password.reset')}" method="reset" />
  <ww:submit value="%{getText('cancel')}" method="cancel" />
</ww:form>

</body>
</ww:i18n>
</html>

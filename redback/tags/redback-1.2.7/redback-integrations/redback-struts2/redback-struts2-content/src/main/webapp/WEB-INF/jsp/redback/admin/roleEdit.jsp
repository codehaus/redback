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

<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>

<html>
<s:i18n name="org.codehaus.plexus.redback.struts2.default">
<head>
  <title><s:text name="role.page.title"/></title>
</head>

<body>

  <%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

  <h2><s:text name="role"/></h2>

  <s:actionerror/>

  <s:form action="rolesave" name="rolesave" namespace="/security">
    <s:hidden name="name"/>
    <s:hidden name="usersList"/>
    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <s:label label="%{getText('name')}" name="name"/>
        <s:textfield label="%{getText('description')}" name="newDescription" maxlength="255"/>
      </table>
    </div>
    <div class="functnbar3">
        <s:submit value="%{getText('save')}" />
    </div>
  </s:form>

  <h3><s:text name="role.model.parent.roles"/></h3>
  <c:if test="${empty parentRoleNames}">
    <s:text name="role.edit.no.parent.defined"/>
  </c:if>
  <c:if test="${!empty parentRoleNames}">
    <ul>
    <s:iterator id="parentRoleName" value="parentRoleNames">
      <s:url id="roleeditUrl" action="roleedit" includeParams="get">
        <s:param name="name">${parentRoleName}</s:param>
      </s:url>
      <li><s:a href="%{roleeditUrl}">${parentRoleName}</s:a></li>
    </s:iterator>
    </ul>
  </c:if>

  <h3><s:text name="role.model.child.roles"/></h3>
  <c:if test="${empty childRoleNames}">
    <s:text name="role.edit.no.childrole.defined"/>
  </c:if>
  <c:if test="${!empty childRoleNames}">
    <ul>
    <s:iterator id="childRoleName" value="childRoleNames">
      <s:url id="roleeditUrl" action="roleedit" includeParams="get">
        <s:param name="name">${childRoleName}</s:param>
      </s:url>
      <li><s:a href="%{roleeditUrl}">${childRoleName}</s:a></li>
    </s:iterator>
    </ul>
  </c:if>

  <h3><s:text name="permissions"/></h3>
  <c:if test="${empty permissions}">
    <s:text name="role.create.no.permissions.defined"/>
  </c:if>
  <c:if test="${!empty permissions}">
    <!-- ec on redback should not be dependent on the resource bundle of the project using it -->
	<s:set name="role_name" value="%{getText('name')}"/>
	<s:set name="role_operation" value="%{getText('role.operation')}"/>
	<s:set name="role_resource" value="%{getText('role.resource')}"/>
    <ec:table var="permission"
        items="permissions"
        autoIncludeParameters="false"
        cellspacing="2"
        cellpadding="3"
        showExports="flase"
        showPagination="false"
        showTitle="false"
        showStatusBar="false"
        filterable="false">
      <ec:row>
        <ec:column property="name" title="${role_name}"/>
        <ec:column property="operation.name" title="${role_operation}"/>
        <ec:column property="resource.identifier" title="${role_resource}"/>
      </ec:row>
    </ec:table>
  </c:if>

  <h3><s:text name="role.edit.section.users"/></h3>

  <c:if test="${!empty parentUsers}">
    <h4><s:text name="role.edit.users.defined.in.parent.roles"/></h4>
    <ul>
      <s:iterator id="user" value="parentUsers">
        <s:url id="usereditUrl" action="useredit" includeParams="get">
          <s:param name="username">${user.username}</s:param>
        </s:url>
        <li><s:a href="%{usereditUrl}">${user.fullName} (${user.username} - ${user.email})</s:a></li>
      </s:iterator>
    </ul>
  </c:if>
  <h4><s:text name="role.edit.users.defined.in.current.role"/></h4>
  <form name="roleusers">
    <input type="hidden" name="name" value="${name}"/>
  <table style="width:100%">
    <tr>
      <td style="width:40%">
        <select size="20" multiple="multiple" id="allUsers" name="availableUsers" style="width: 100%">
          <s:iterator id="user" value="allUsers">
            <option value="${user.username}">${user.fullName} - ${user.username}</option>
          </s:iterator>
        </select>
      </td>
      <td style="text-align: center">
        <input type="submit" value="--&gt;" onclick="this.form.action='roleusersadd.action'; this.form.submit();"/>
        <br/>
        <input type="submit" value="&lt;--" onclick="this.form.action='roleusersremove.action'; this.form.submit();"/>
      </td>
      <td style="width:40%">
        <c:if test="${!empty users}">
          <select size="20" multiple="multiple" id="users" name="currentUsers" style="width: 100%">
            <s:iterator id="user" value="users">
              <option value="${user.username}">${user.fullName} - ${user.username}</option>
            </s:iterator>
          </select>
        </c:if>
        <c:if test="${empty users}">
          <select size="20" multiple="multiple" id="users" name="usersList" style="width: 100%">
          </select>
        </c:if>
      </td>
    </tr>
  </table>
  </form>

</body>
</s:i18n>
</html>
          
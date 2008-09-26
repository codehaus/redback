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

<%@ taglib prefix="ww" uri="/struts-tags"%>
<%@ taglib prefix="redback" uri="http://plexus.codehaus.org/redback/taglib-1.0"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<ww:i18n name="org.codehaus.plexus.redback.struts2.default">
<head>
  <title><ww:text name="role.list.page.title"/></title>
</head>

<body>

<!-- %@ include file="/WEB-INF/jsp/redback/include/rbacListNavigation.jsp" % -->

<h2><ww:text name="role.list.section.title"/></h2>

<%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

  <table width="100%">

    <c:choose>
      <c:when test="${!empty allRoles}">
        <thead>
          <tr>
            <th width="49%"><ww:text name="role.name"/></th>
            <th width="49%"><ww:text name="role.description"/></th>
          </tr>
        </thead>
        
        <c:forEach var="role" items="${allRoles}">
          <tr>
            <td>
              <ww:url id="roleUrl" action="role">
                <ww:param name="name">${role.name}</ww:param>
              </ww:url>
              <ww:a href="%{roleUrl}">${role.name}</ww:a>
            </td>
            <td>
              <c:out value="${role.description}" />
            </td>
          </tr>
        </c:forEach>
      </c:when>
      <c:otherwise>
        <p><em><ww:text name="role.list.no.roles.available"/></em></p>
      </c:otherwise>
    </c:choose>
  </table>

</body>
</ww:i18n>
</html>

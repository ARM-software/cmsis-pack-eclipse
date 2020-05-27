<#compress>
<#include "helper.ftlinc"/>
<#assign regions={}/>
<#list zone as z>
  <#list z.memory as m>
    <#if !regions?keys?seq_contains(m.name)>
      <#assign regions += { m.name: m }>
    </#if>
  </#list> 
</#list>
</#compress>
#ifndef MEM_LAYOUT_H
#define MEM_LAYOUT_H

<#list regions?values as r>
#define REGION_${r.name?upper_case}_START ${r.start}
#define REGION_${r.name?upper_case}_SIZE  ${r.size}
</#list>

#endif

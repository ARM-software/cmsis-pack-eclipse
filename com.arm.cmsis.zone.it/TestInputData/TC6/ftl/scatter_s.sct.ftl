<#compress>
<#include "helper.ftlinc"/>
<#-- Prepare code and data regions -->
<#assign code={}>
<#assign data={}>
<#list zone as z>
  <#if z.security.s == "1">
    <#list z.memory as m>
    	<#if m.access.w?number == 1>
        <#if !data?keys?seq_contains(m.name)>
          <#assign data += { m.name: m }>
        </#if>
    	<#else>
        <#if !code?keys?seq_contains(m.name)>
          <#assign code += { m.name: m }>
        </#if>
      </#if>
    </#list>
  </#if>  
</#list>
</#compress>
<#assign resetRegion = false/>
<#assign veneerRegion = false/>
<#assign dataRegions = false/>
<#-- Static scatter file content with config wizard annotations -->
#! armclang -E --target=arm-arm-none-eabi -mcpu=cortex-m33 -I../ -xc

#include "mem_layout.h"

; *------- <<< Use Configuration Wizard in Context Menu >>> ------------------

;<h> Stack Configuration
;  <o> Stack Size (in Bytes) <0x0-0xFFFFFFFF:8>
;</h>
#define STACK_SIZE 0x400

;<h> Heap Configuration
;  <o> Heap Size (in Bytes) <0x0-0xFFFFFFFF:8>
;</h>
#define HEAP_SIZE 0xC00

; *-------------- <<< end of configuration section >>> -----------------------
<#-- Generate code regions -->
<#list code?values?sort_by("start") as m>
LR_${m.name} REGION_${m.name?upper_case}_START REGION_${m.name?upper_case}_SIZE {
<#if m.access.x?number == 1>
  ER_${m.name} REGION_${m.name?upper_case}_START REGION_${m.name?upper_case}_SIZE {
  <#-- Add reset vector and root sections to first code regiono -->
  <#if !resetRegion>
  	<#assign resetRegion = true/>
  	* (RESET,+FIRST)
    * (InRoot$$Sections)
    .ANY (+RO +XO)
  </#if>
  <#-- Add all secure gateways to first veneer region -->
  <#if m.security.c?number == 1 && !veneerRegion>
    <#assign veneerRegion = true/>
  	*(Veneer$$CMSE)
  </#if>
  }
</#if>
<#-- Add all data regions to first load region -->
<#if !dataRegions>
  <#assign dataRegions = true/>
  <#list data?values?sort_by("start") as m>
    <#if m?index = 0>
  RW_${m.name} REGION_${m.name?upper_case}_START REGION_${m.name?upper_case}_SIZE-HEAP_SIZE-STACK_SIZE {
    .ANY (+RW +ZI)
  }
<#-- Add heap and stack regions to first load region -->
#if HEAP_SIZE>0
  ARM_LIB_HEAP REGION_${m.name?upper_case}_START+REGION_${m.name?upper_case}_SIZE-HEAP_SIZE-STACK_SIZE EMPTY HEAP_SIZE {
  }
#endif
#if STACK_SIZE>0
  ARM_LIB_STACK REGION_${m.name?upper_case}_START+REGION_${m.name?upper_case}_SIZE-STACK_SIZE EMPTY STACK_SIZE {
  }
#endif  
    <#else>
  RW_${m.name} REGION_${m.name?upper_case}_START REGION_${m.name?upper_case}_SIZE {
    .ANY (+RW +ZI)
  }
    </#if>
  </#list>
</#if>
}
</#list>

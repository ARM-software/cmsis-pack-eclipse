<#compress>
<#include "helper.ftlinc"/>

<#-- Prepare Non-Secure Interrupt Entries -->
<#assign maxirq = 0/>
<#assign it = {}/>
<#assign itns = {}/>

<#if system.interrupt?has_content>
  <#list system.interrupt?sort_by("irqn") as irq>
    <#assign it += { irq.irqn?number : irq } />  
    <#if (irq.irqn?number > maxirq)>
      <#assign maxirq = irq.irqn?number>
    </#if>
    <#if irq.security.n == "1">
      <#assign itns += { irq.irqn?number : irq } />
    </#if>
  </#list>
</#if>
</#compress>
/*
 * Copyright 2019 Arm Limited
 *
 * SPDX-License-Identifier: Apache 2.0
 */

#include "tzm_config.h" 
#include "fsl_common.h"

void TZM_Config_SAU(void)
{
    /* Disable SAU */
    SAU->CTRL = 0U;
    
<#list system.sau as region>
    /* Configure SAU region ${region?index} - ${region.info} */
    /* Set SAU region number */
    SAU->RNR = ${region?index};
    /* Region base address */   
    SAU->RBAR = ${region.start};
    /* Region end address */
    SAU->RLAR = (${region.end} & SAU_RLAR_LADDR_Msk) | 
                 /* Region memory attribute index */
                 ((${region.nsc}U << SAU_RLAR_NSC_Pos) & SAU_RLAR_NSC_Msk) |
                 /* Enable region */
                 ((1U << SAU_RLAR_ENABLE_Pos) & SAU_RLAR_ENABLE_Msk);
</#list>
                 
     /* Force memory writes before continuing */
    __DSB();
    /* Flush and refill pipeline with updated permissions */
    __ISB();     
    /* Enable SAU */
    SAU->CTRL = 1U;

   /* Interrupt configuration */
  <#list 0..(maxirq/32)?floor as i>
    <#assign itns_val = 0>
    <#assign itns_com = []>
    <#list 0..31 as j>
      <#if itns?keys?seq_contains((i * 32 + j)?c)>
        <#assign itns_val += pow2(j)>
        <#assign itns_com += [ itns[(i * 32 + j)?c].name ] >
      </#if>
    </#list>
    <#if itns_val!=0>
    NVIC->ITNS[${i}] = ${num2hex(itns_val, "0x", 8)}; // ${itns_com?reverse?join(", ")}
    </#if>
  </#list>   
}

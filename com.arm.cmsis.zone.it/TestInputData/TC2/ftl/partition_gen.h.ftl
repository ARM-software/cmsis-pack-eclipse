<#compress>
<#include "helper.ftlinc"/>

<#assign aDateTime = .now>

<#-- Prepare SAU entries -->
<#assign sau_entries = 0>
<#assign sau_table = []/>

<#if system.sau?has_content>
  <#list system.sau?sort_by("start") as s>
    <#assign sau_table += [{"start_address":s.start,      "end_address":s.end,        "nsc":s.nsc, "init":1}]/>
    <#assign sau_entries += 1>
  </#list>
</#if>
  <#list sau_entries..7 as i>
    <#assign sau_table += [{"start_address":"0x00000000", "end_address":"0x00000000", "nsc":0,     "init":0}]/>
  </#list>

<#-- Prepare Non-Secure Interrupt Entries -->
<#assign itns_entries = 0>
<#assign itns_table = []/>
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

<#list 0..(maxirq/32)?floor as i>
  <#assign itns_val  = 0 />
  <#assign itns_init = 0 />
  <#list 0..31 as j>
    <#if itns?keys?seq_contains((i * 32 + j)?c)>
      <#assign itns_init = 1 />
      <#assign itns_val += pow2(j) />
    </#if>
  </#list>
  <#assign itns_table += [{"val" : num2hex(itns_val, "0x", 8), "init" : itns_init}] />
</#list>

<#-- Collect all assigned memories -->
<#function memories zones>
  <#assign mems={} />
  <#list zone as z>
    <#list z.memory as m>
      <#if !mems?keys?seq_contains(m.name)>
        <#assign mems += { m.name : m } />
      </#if>
    </#list>
  </#list>
  <#return mems />
</#function>

<#-- Check non-secure attribute of memory -->
<#function isNonsecure memory>
  <#return memory.security.n == "1" />
</#function>

<#-- Check memory start address is in Flash area -->
<#function isFlash memory>
  <#return hex2num(memory.start)<536870912 />
</#function>

<#-- Calculate non-secure base address (NSBA) -->
<#function fcmNsba zones>
  <#assign nsflash = filter(filter(memories(zones)?values, isNonsecure), isFlash)?sort_by("start") />
  <#if nsflash?size gt 0> 
    <#return hex2num(nsflash[0].start) % 268435456 />
  <#else>
    <#return 268435455 />
  </#if> 
</#function>

<#-- Collect all MPC register values -->
<#function mpcRegs mpc>
  <#assign regs = [] />
  <#list 0..((mpc.S_bit?size-1)/32) as i>
    <#assign reg = 0 />
    <#list 0..31 as j>
      <#assign reg += (((mpc.S_bit[i*32+j]!"1")?number+1)%2)*pow2(j) />
    </#list>
    <#assign regs += [reg] />
  </#list>
  <#return regs />
</#function>
</#compress>
/**************************************************************************//**
 * @file     partition_gen.h
 * @brief    CMSIS-Core(M) Initial Setup for Secure / Non-Secure Zones for 
 *           Nuvoton M2351
 * @version  V1.0.0      (FreeMarker generated)
 * @date     ${aDateTime?date} ${aDateTime?time}
 ******************************************************************************/
/*
 * Copyright (c) 2009-2019 Arm Limited. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the License); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef PARTITION_GEN_H
#define PARTITION_GEN_H

/*
//-------- <<< Use Configuration Wizard in Context Menu >>> -----------------
*/

/*
// <e>Initialize Non-secure Base Address
*/
#define FMC_INIT_NSBA          1

/*
//     <o>Secure Flash ROM Size <0x800-0x7FFFF:0x800>
*/
#define FMC_SECURE_ROM_SIZE      ${num2hex(fcmNsba(system.zone))}

#define FMC_NON_SECURE_BASE     (0x10000000 + FMC_SECURE_ROM_SIZE)

/*
// </e>
*/

/*
// <h>Initialize Security Attribution Unit (SAU) Address Regions
// <i>SAU configuration specifies regions to be one of:
// <i> - Secure and Non-Secure Callable
// <i> - Non-Secure
// <i>Note: All memory regions not configured by SAU are Secure
*/
#define SAU_REGIONS_MAX   ${sau_table?size}   /* Max. number of SAU regions */

<#list sau_table as sau_entry>
/*
//   <e>Initialize SAU Region ${sau_entry?index}
//   <i> Setup SAU Region  ${sau_entry?index} memory attributes
*/
#define SAU_INIT_REGION${sau_entry?index}       ${sau_entry.init}

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START${sau_entry?index}       ${sau_entry.start_address}      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END${sau_entry?index}       ${sau_entry.end_address}      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC${sau_entry?index}       ${sau_entry.nsc}
/*
//   </e>
*/
</#list>

/*
// </h>
*/

/*
// <h>Setup Interrupt Target
*/

<#list itns_table as itns_entry>

/*
//   <e>Initialize ITNS ${itns_entry?index} (Interrupts ${itns_entry?index*32}..${itns_entry?index*32+31})
*/
#define NVIC_INIT_ITNS${itns_entry?index}    ${itns_entry.init}

/*
// Interrupts ${itns_entry?index*32}..${itns_entry?index*32+31}
<#list 0..31 as i>
//   <o.${i}>  Interrupt ${(itns_entry?index*32+i)?left_pad(3)}<#if it?keys?seq_contains((itns_entry?index*32+i)?c)>: ${it[(itns_entry?index*32+i)?c].name}</#if> <0=> Secure state <1=> Non-Secure state
</#list>
*/
#define NVIC_INIT_ITNS${itns_entry?index}_VAL      ${itns_entry.val}

/*
//   </e>
*/
</#list>

/*
// </h>
*/

/**
  \brief   Setup SCU Configuration Unit
  \details

 */
__STATIC_INLINE void SCU_Setup(void)
{
  /* Setup Peripheral Protection Controller (PPC) */
  <#list system.reg_setup as reg>
    <#assign value = 0 />
    <#list reg.value as v>
      <#assign value += hex2num(v) />
    </#list>
    ${reg.name} = ${num2hex(value)}U;
  </#list>

  /* Setup Memory Protection Controller (MPC) */
  <#list system.mpc_setup as mpc>
    /* ${mpc.info} */
    <#assign regs = mpcRegs(mpc)>
    <#if regs?size == 1>
      <#list regs as reg>
    ${mpc.name} = ${num2hex(reg)}U;
      </#list>
    <#else>
      <#list regs as reg>
    ${mpc.name}[${reg?index}] = ${num2hex(reg)}U;
      </#list>
    </#if>
  </#list>
}

#endif  /* PARTITION_H */

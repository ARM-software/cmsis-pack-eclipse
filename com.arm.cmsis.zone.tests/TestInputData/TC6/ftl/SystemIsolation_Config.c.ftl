<#compress>
<#include "helper.ftlinc"/>

<#-- Generation bit list for Memory Protection Controller -->
<#function mpcRegs mpc>
  <#assign regs = [] />
  <#list 0..((mpc.S_bit?size-1)/32) as i>
    <#assign reg = 0 />
    <#list 0..31 as j>
      <#assign reg += (mpc.S_bit[i*32+j]!"0")?number*pow2(j) />
    </#list>
    <#assign regs += [reg] />
  </#list>
  <#return regs />
</#function>
</#compress>
/*----------------------------------------------------------------------------
  System Isolation Configuration
  This function is responsible for Memory and Peripheral isolation
  for secure and non-secure application parts
 *----------------------------------------------------------------------------*/

#include "stm32l5xx_hal.h"

void SystemIsolation_Config(void)
{
  /* Enable GTZC peripheral clock */
  __HAL_RCC_GTZC_CLK_ENABLE();

  /* Setup Memory Protection Controller (MPC) */
<#list system.mpc_setup as mpc>
  /* ${mpc.info} */
  <#list mpcRegs(mpc) as reg>
  WRITE_REG(${mpc.name}->VCTR[${reg?index}], ${num2hex(reg)}U);
  </#list>
</#list>
  
  /* Setup Peripheral Protection Controller (PPC) */
<#list system.reg_setup as reg>
  WRITE_REG(${reg.name},
  <#list reg.value as v>
    ${v} <#if v?has_next>|<#else> </#if> /* ${reg.peripheral[v?index]} */
  </#list>
  );
</#list>

  /* Clear all illegal access pending interrupts in TZIC */
  if (HAL_GTZC_TZIC_ClearFlag(GTZC_PERIPH_ALL) != HAL_OK)
  {
    /* Initialization Error */
    while(1);
  }

  /* Enable all illegal access interrupts in TZIC */
  if(HAL_GTZC_TZIC_EnableIT(GTZC_PERIPH_ALL) != HAL_OK)
  {
    /* Initialization Error */
    while(1);
  }

  /* Enable TZIC secure interrupt */
  HAL_NVIC_SetPriority(TZIC_S_IRQn, 0, 0); /* Highest priority level */
  HAL_NVIC_ClearPendingIRQ(TZIC_S_IRQn);
  HAL_NVIC_EnableIRQ(TZIC_S_IRQn);
}

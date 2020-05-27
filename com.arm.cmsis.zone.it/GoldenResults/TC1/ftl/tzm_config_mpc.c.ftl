<#compress>
<#include "helper.ftlinc"/>

<#function mpcRegs mpc>
	<#assign regs = [] />
	<#list 0..((mpc.S_bit?size-1)/8) as i>
		<#assign reg = 0 />
		<#list 0..7 as j>
			<#assign reg += (mpc.S_bit[i*8+j]!"0")?number*pow2(j*4)   />
			<#assign reg += (mpc.P_bit[i*8+j]!"0")?number*pow2(j*4+1) />
		</#list>
		<#assign regs += [reg] />
	</#list>
	<#return regs />
</#function>
</#compress>
/*
 * Copyright 2019 Arm Limited
 *
 * SPDX-License-Identifier: Apache 2.0
 */

#include "tzm_config.h" 
#include "fsl_common.h"

void TZM_Config_MPC(void)
{
  /* Setup Memory Protection Controller (MPC) */
<#list system.mpc_setup as mpc>
	/* ${mpc.info} */
	<#list mpcRegs(mpc) as reg>
	${mpc.name}[${reg?index}] = ${num2hex(reg)}U;
	</#list>
</#list>
}

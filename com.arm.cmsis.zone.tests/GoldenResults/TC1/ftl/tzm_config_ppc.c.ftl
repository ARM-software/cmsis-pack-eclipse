<#compress>
<#include "helper.ftlinc"/>
</#compress>
/*
 * Copyright 2019 Arm Limited
 *
 * SPDX-License-Identifier: Apache 2.0
 */

#include "tzm_config.h"
#include "fsl_common.h"

void TZM_Config_PPC(void)
{
  /* Setup Peripheral Protection Controller (PPC) */
<#list system.reg_setup as reg>
	<#assign value = 0 />
	<#list reg.value as v>
		<#assign value += hex2num(v) />
	</#list>
	${reg.name} = ${num2hex(value)}U;
</#list>
}

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
	AHB_SECURE_CTRL->SEC_CTRL_AHB0_0_SLAVE_RULE = 0x02000000U;
	AHB_SECURE_CTRL->SEC_CTRL_APB_BRIDGE[0].SEC_CTRL_APB_BRIDGE0_MEM_CTRL0 = 0x00000022U;
}

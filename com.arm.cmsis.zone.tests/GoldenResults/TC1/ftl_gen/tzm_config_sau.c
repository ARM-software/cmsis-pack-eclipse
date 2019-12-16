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
    
    /* Configure SAU region 0 - memory:CODE_NS */
    /* Set SAU region number */
    SAU->RNR = 0;
    /* Region base address */   
    SAU->RBAR = 0x00010000;
    /* Region end address */
    SAU->RLAR = (0x00081FE0 & SAU_RLAR_LADDR_Msk) | 
                 /* Region memory attribute index */
                 ((0U << SAU_RLAR_NSC_Pos) & SAU_RLAR_NSC_Msk) |
                 /* Enable region */
                 ((1U << SAU_RLAR_ENABLE_Pos) & SAU_RLAR_ENABLE_Msk);
    /* Configure SAU region 1 - memory:Veneer */
    /* Set SAU region number */
    SAU->RNR = 1;
    /* Region base address */   
    SAU->RBAR = 0x1000FE00;
    /* Region end address */
    SAU->RLAR = (0x1000FFE0 & SAU_RLAR_LADDR_Msk) | 
                 /* Region memory attribute index */
                 ((1U << SAU_RLAR_NSC_Pos) & SAU_RLAR_NSC_Msk) |
                 /* Enable region */
                 ((1U << SAU_RLAR_ENABLE_Pos) & SAU_RLAR_ENABLE_Msk);
    /* Configure SAU region 2 - memory:DATA_NS */
    /* Set SAU region number */
    SAU->RNR = 2;
    /* Region base address */   
    SAU->RBAR = 0x20008000;
    /* Region end address */
    SAU->RLAR = (0x20043FE0 & SAU_RLAR_LADDR_Msk) | 
                 /* Region memory attribute index */
                 ((0U << SAU_RLAR_NSC_Pos) & SAU_RLAR_NSC_Msk) |
                 /* Enable region */
                 ((1U << SAU_RLAR_ENABLE_Pos) & SAU_RLAR_ENABLE_Msk);
                 
     /* Force memory writes before continuing */
    __DSB();
    /* Flush and refill pipeline with updated permissions */
    __ISB();     
    /* Enable SAU */
    SAU->CTRL = 1U;

   /* Interrupt configuration */
    NVIC->ITNS[0] = 0x2040000C; // RTC, ADC, GINT1, GINT0
}

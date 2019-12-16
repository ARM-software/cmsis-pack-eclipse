/**************************************************************************//**
 * @file     partition_gen.h
 * @brief    CMSIS-CORE Initial Setup for Secure / Non-Secure Zones for STM32L5xx Device
 * @version  V1.0.0      (FreeMarker generated)
 * @date     Dec 13, 2019 11:53:50 AM
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
// <h>Initialize Security Attribution Unit (SAU) Address Regions
// <i>SAU configuration specifies regions to be one of:
// <i> - Secure and Non-Secure Callable
// <i> - Non-Secure
// <i>Note: All memory regions not configured by SAU are Secure
*/
#define SAU_REGIONS_MAX   8 	/* Max. number of SAU regions */

/*
//   <e>Initialize SAU Region 0
//   <i> Setup SAU Region  0 memory attributes
*/
#define SAU_INIT_REGION0       1

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START0       0x08040000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END0       0x0807FFE0      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC0       0
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 1
//   <i> Setup SAU Region  1 memory attributes
*/
#define SAU_INIT_REGION1       1

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START1       0x0C03E000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END1       0x0C03FFE0      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC1       1
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 2
//   <i> Setup SAU Region  2 memory attributes
*/
#define SAU_INIT_REGION2       1

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START2       0x20000000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END2       0x2001FFE0      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC2       0
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 3
//   <i> Setup SAU Region  3 memory attributes
*/
#define SAU_INIT_REGION3       1

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START3       0x40000000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END3       0x4FFFFFE0      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC3       0
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 4
//   <i> Setup SAU Region  4 memory attributes
*/
#define SAU_INIT_REGION4       0

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START4       0x00000000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END4       0x00000000      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC4       0
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 5
//   <i> Setup SAU Region  5 memory attributes
*/
#define SAU_INIT_REGION5       0

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START5       0x00000000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END5       0x00000000      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC5       0
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 6
//   <i> Setup SAU Region  6 memory attributes
*/
#define SAU_INIT_REGION6       0

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START6       0x00000000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END6       0x00000000      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC6       0
/*
//   </e>
*/
/*
//   <e>Initialize SAU Region 7
//   <i> Setup SAU Region  7 memory attributes
*/
#define SAU_INIT_REGION7       0

/*
//     <o>Start Address <0-0xFFFFFFE0>
*/
#define SAU_INIT_START7       0x00000000      /* start address of SAU region 0 */

/*
//     <o>End Address <0x1F-0xFFFFFFFF>
*/
#define SAU_INIT_END7       0x00000000      /* end address of SAU region 0 */

/*
//     <o>Region is
//         <0=>Non-Secure
//         <1=>Secure, Non-Secure Callable
*/
#define SAU_INIT_NSC7       0
/*
//   </e>
*/

/*
// </h>
*/

/*
// <h>Setup Interrupt Target
*/


/*
//   <e>Initialize ITNS 0 (Interrupts 0..31)
*/
#define NVIC_INIT_ITNS0    0

/*
// Interrupts 0..31
//   <o.0>  Interrupt   0 <0=> Secure state <1=> Non-Secure state
//   <o.1>  Interrupt   1 <0=> Secure state <1=> Non-Secure state
//   <o.2>  Interrupt   2 <0=> Secure state <1=> Non-Secure state
//   <o.3>  Interrupt   3 <0=> Secure state <1=> Non-Secure state
//   <o.4>  Interrupt   4 <0=> Secure state <1=> Non-Secure state
//   <o.5>  Interrupt   5 <0=> Secure state <1=> Non-Secure state
//   <o.6>  Interrupt   6 <0=> Secure state <1=> Non-Secure state
//   <o.7>  Interrupt   7 <0=> Secure state <1=> Non-Secure state
//   <o.8>  Interrupt   8 <0=> Secure state <1=> Non-Secure state
//   <o.9>  Interrupt   9 <0=> Secure state <1=> Non-Secure state
//   <o.10>  Interrupt  10 <0=> Secure state <1=> Non-Secure state
//   <o.11>  Interrupt  11 <0=> Secure state <1=> Non-Secure state
//   <o.12>  Interrupt  12 <0=> Secure state <1=> Non-Secure state
//   <o.13>  Interrupt  13 <0=> Secure state <1=> Non-Secure state
//   <o.14>  Interrupt  14 <0=> Secure state <1=> Non-Secure state
//   <o.15>  Interrupt  15 <0=> Secure state <1=> Non-Secure state
//   <o.16>  Interrupt  16 <0=> Secure state <1=> Non-Secure state
//   <o.17>  Interrupt  17 <0=> Secure state <1=> Non-Secure state
//   <o.18>  Interrupt  18 <0=> Secure state <1=> Non-Secure state
//   <o.19>  Interrupt  19 <0=> Secure state <1=> Non-Secure state
//   <o.20>  Interrupt  20 <0=> Secure state <1=> Non-Secure state
//   <o.21>  Interrupt  21 <0=> Secure state <1=> Non-Secure state
//   <o.22>  Interrupt  22 <0=> Secure state <1=> Non-Secure state
//   <o.23>  Interrupt  23 <0=> Secure state <1=> Non-Secure state
//   <o.24>  Interrupt  24 <0=> Secure state <1=> Non-Secure state
//   <o.25>  Interrupt  25 <0=> Secure state <1=> Non-Secure state
//   <o.26>  Interrupt  26 <0=> Secure state <1=> Non-Secure state
//   <o.27>  Interrupt  27 <0=> Secure state <1=> Non-Secure state
//   <o.28>  Interrupt  28 <0=> Secure state <1=> Non-Secure state
//   <o.29>  Interrupt  29: DMA1_Channel1 <0=> Secure state <1=> Non-Secure state
//   <o.30>  Interrupt  30: DMA1_Channel2 <0=> Secure state <1=> Non-Secure state
//   <o.31>  Interrupt  31: DMA1_Channel3 <0=> Secure state <1=> Non-Secure state
*/
#define NVIC_INIT_ITNS0_VAL      0x00000000

/*
//   </e>
*/

/*
//   <e>Initialize ITNS 1 (Interrupts 32..63)
*/
#define NVIC_INIT_ITNS1    1

/*
// Interrupts 32..63
//   <o.0>  Interrupt  32: DMA1_Channel4 <0=> Secure state <1=> Non-Secure state
//   <o.1>  Interrupt  33: DMA1_Channel5 <0=> Secure state <1=> Non-Secure state
//   <o.2>  Interrupt  34: DMA1_Channel6 <0=> Secure state <1=> Non-Secure state
//   <o.3>  Interrupt  35: DMA1_Channel7 <0=> Secure state <1=> Non-Secure state
//   <o.4>  Interrupt  36: DMA1_Channel8 <0=> Secure state <1=> Non-Secure state
//   <o.5>  Interrupt  37: ADC1_2 <0=> Secure state <1=> Non-Secure state
//   <o.6>  Interrupt  38 <0=> Secure state <1=> Non-Secure state
//   <o.7>  Interrupt  39: FDCAN1_IT0 <0=> Secure state <1=> Non-Secure state
//   <o.8>  Interrupt  40: FDCAN1_IT1 <0=> Secure state <1=> Non-Secure state
//   <o.9>  Interrupt  41: TIM1_BRK <0=> Secure state <1=> Non-Secure state
//   <o.10>  Interrupt  42: TIM1_UP <0=> Secure state <1=> Non-Secure state
//   <o.11>  Interrupt  43: TIM1_TRG_COM <0=> Secure state <1=> Non-Secure state
//   <o.12>  Interrupt  44: TIM1_CC <0=> Secure state <1=> Non-Secure state
//   <o.13>  Interrupt  45: TIM2 <0=> Secure state <1=> Non-Secure state
//   <o.14>  Interrupt  46: TIM3 <0=> Secure state <1=> Non-Secure state
//   <o.15>  Interrupt  47: TIM4 <0=> Secure state <1=> Non-Secure state
//   <o.16>  Interrupt  48: TIM5 <0=> Secure state <1=> Non-Secure state
//   <o.17>  Interrupt  49: TIM6 <0=> Secure state <1=> Non-Secure state
//   <o.18>  Interrupt  50: TIM7 <0=> Secure state <1=> Non-Secure state
//   <o.19>  Interrupt  51: TIM8_BRK <0=> Secure state <1=> Non-Secure state
//   <o.20>  Interrupt  52: TIM8_UP <0=> Secure state <1=> Non-Secure state
//   <o.21>  Interrupt  53: TIM8_TRG_COM <0=> Secure state <1=> Non-Secure state
//   <o.22>  Interrupt  54: TIM8_CC <0=> Secure state <1=> Non-Secure state
//   <o.23>  Interrupt  55: I2C1_EV <0=> Secure state <1=> Non-Secure state
//   <o.24>  Interrupt  56: I2C1_ER <0=> Secure state <1=> Non-Secure state
//   <o.25>  Interrupt  57: I2C2_EV <0=> Secure state <1=> Non-Secure state
//   <o.26>  Interrupt  58: I2C2_ER <0=> Secure state <1=> Non-Secure state
//   <o.27>  Interrupt  59: SPI1 <0=> Secure state <1=> Non-Secure state
//   <o.28>  Interrupt  60: SPI2 <0=> Secure state <1=> Non-Secure state
//   <o.29>  Interrupt  61: USART1 <0=> Secure state <1=> Non-Secure state
//   <o.30>  Interrupt  62: USART2 <0=> Secure state <1=> Non-Secure state
//   <o.31>  Interrupt  63: USART3 <0=> Secure state <1=> Non-Secure state
*/
#define NVIC_INIT_ITNS1_VAL      0x40008020

/*
//   </e>
*/

/*
//   <e>Initialize ITNS 2 (Interrupts 64..95)
*/
#define NVIC_INIT_ITNS2    0

/*
// Interrupts 64..95
//   <o.0>  Interrupt  64: UART4 <0=> Secure state <1=> Non-Secure state
//   <o.1>  Interrupt  65: UART5 <0=> Secure state <1=> Non-Secure state
//   <o.2>  Interrupt  66: LPUART1 <0=> Secure state <1=> Non-Secure state
//   <o.3>  Interrupt  67: LPTIM1 <0=> Secure state <1=> Non-Secure state
//   <o.4>  Interrupt  68: LPTIM2 <0=> Secure state <1=> Non-Secure state
//   <o.5>  Interrupt  69: TIM15 <0=> Secure state <1=> Non-Secure state
//   <o.6>  Interrupt  70: TIM16 <0=> Secure state <1=> Non-Secure state
//   <o.7>  Interrupt  71: TIM17 <0=> Secure state <1=> Non-Secure state
//   <o.8>  Interrupt  72: COMP <0=> Secure state <1=> Non-Secure state
//   <o.9>  Interrupt  73 <0=> Secure state <1=> Non-Secure state
//   <o.10>  Interrupt  74 <0=> Secure state <1=> Non-Secure state
//   <o.11>  Interrupt  75 <0=> Secure state <1=> Non-Secure state
//   <o.12>  Interrupt  76: OCTOSPI1 <0=> Secure state <1=> Non-Secure state
//   <o.13>  Interrupt  77 <0=> Secure state <1=> Non-Secure state
//   <o.14>  Interrupt  78 <0=> Secure state <1=> Non-Secure state
//   <o.15>  Interrupt  79 <0=> Secure state <1=> Non-Secure state
//   <o.16>  Interrupt  80: DMA2_CH1 <0=> Secure state <1=> Non-Secure state
//   <o.17>  Interrupt  81: DMA2_CH2 <0=> Secure state <1=> Non-Secure state
//   <o.18>  Interrupt  82: DMA2_CH3 <0=> Secure state <1=> Non-Secure state
//   <o.19>  Interrupt  83: DMA2_CH4 <0=> Secure state <1=> Non-Secure state
//   <o.20>  Interrupt  84: DMA2_CH5 <0=> Secure state <1=> Non-Secure state
//   <o.21>  Interrupt  85: DMA2_CH6 <0=> Secure state <1=> Non-Secure state
//   <o.22>  Interrupt  86: DMA2_CH7 <0=> Secure state <1=> Non-Secure state
//   <o.23>  Interrupt  87: DMA2_CH8 <0=> Secure state <1=> Non-Secure state
//   <o.24>  Interrupt  88: I2C3_EV <0=> Secure state <1=> Non-Secure state
//   <o.25>  Interrupt  89: I2C3_ER <0=> Secure state <1=> Non-Secure state
//   <o.26>  Interrupt  90: SAI1 <0=> Secure state <1=> Non-Secure state
//   <o.27>  Interrupt  91: SAI2 <0=> Secure state <1=> Non-Secure state
//   <o.28>  Interrupt  92 <0=> Secure state <1=> Non-Secure state
//   <o.29>  Interrupt  93 <0=> Secure state <1=> Non-Secure state
//   <o.30>  Interrupt  94 <0=> Secure state <1=> Non-Secure state
//   <o.31>  Interrupt  95 <0=> Secure state <1=> Non-Secure state
*/
#define NVIC_INIT_ITNS2_VAL      0x00000000

/*
//   </e>
*/

/*
//   <e>Initialize ITNS 3 (Interrupts 96..127)
*/
#define NVIC_INIT_ITNS3    0

/*
// Interrupts 96..127
//   <o.0>  Interrupt  96 <0=> Secure state <1=> Non-Secure state
//   <o.1>  Interrupt  97 <0=> Secure state <1=> Non-Secure state
//   <o.2>  Interrupt  98 <0=> Secure state <1=> Non-Secure state
//   <o.3>  Interrupt  99: SPI3 <0=> Secure state <1=> Non-Secure state
//   <o.4>  Interrupt 100: I2C4_ER <0=> Secure state <1=> Non-Secure state
//   <o.5>  Interrupt 101: I2C4_EV <0=> Secure state <1=> Non-Secure state
//   <o.6>  Interrupt 102: DFSDM1_FLT0 <0=> Secure state <1=> Non-Secure state
//   <o.7>  Interrupt 103: DFSDM1_FLT1 <0=> Secure state <1=> Non-Secure state
//   <o.8>  Interrupt 104: DFSDM1_FLT2 <0=> Secure state <1=> Non-Secure state
//   <o.9>  Interrupt 105: DFSDM1_FLT3 <0=> Secure state <1=> Non-Secure state
//   <o.10>  Interrupt 106: UCPD1 <0=> Secure state <1=> Non-Secure state
//   <o.11>  Interrupt 107 <0=> Secure state <1=> Non-Secure state
//   <o.12>  Interrupt 108 <0=> Secure state <1=> Non-Secure state
//   <o.13>  Interrupt 109 <0=> Secure state <1=> Non-Secure state
//   <o.14>  Interrupt 110 <0=> Secure state <1=> Non-Secure state
//   <o.15>  Interrupt 111 <0=> Secure state <1=> Non-Secure state
//   <o.16>  Interrupt 112 <0=> Secure state <1=> Non-Secure state
//   <o.17>  Interrupt 113 <0=> Secure state <1=> Non-Secure state
//   <o.18>  Interrupt 114 <0=> Secure state <1=> Non-Secure state
//   <o.19>  Interrupt 115 <0=> Secure state <1=> Non-Secure state
//   <o.20>  Interrupt 116 <0=> Secure state <1=> Non-Secure state
//   <o.21>  Interrupt 117 <0=> Secure state <1=> Non-Secure state
//   <o.22>  Interrupt 118 <0=> Secure state <1=> Non-Secure state
//   <o.23>  Interrupt 119 <0=> Secure state <1=> Non-Secure state
//   <o.24>  Interrupt 120 <0=> Secure state <1=> Non-Secure state
//   <o.25>  Interrupt 121 <0=> Secure state <1=> Non-Secure state
//   <o.26>  Interrupt 122 <0=> Secure state <1=> Non-Secure state
//   <o.27>  Interrupt 123 <0=> Secure state <1=> Non-Secure state
//   <o.28>  Interrupt 124 <0=> Secure state <1=> Non-Secure state
//   <o.29>  Interrupt 125 <0=> Secure state <1=> Non-Secure state
//   <o.30>  Interrupt 126 <0=> Secure state <1=> Non-Secure state
//   <o.31>  Interrupt 127 <0=> Secure state <1=> Non-Secure state
*/
#define NVIC_INIT_ITNS3_VAL      0x00000000

/*
//   </e>
*/

/*
// </h>
*/

#endif  /* PARTITION_GEN_H */

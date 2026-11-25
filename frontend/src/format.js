const money = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

/** 45 -> "$45.00" */
export const formatMoney = (amount) => money.format(amount)

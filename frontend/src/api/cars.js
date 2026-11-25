// Only the mediator calls these functions. Components never import this file.
import { jsonRequest, request } from './http'

/** Cars that are listed for rent, cheapest first. */
export const getCars = () => request('/api/cars')

/**
 * Lists a car for rent. listing: { year, make, model, mileage, pickupLocation, pricePerDay,
 * availability: [{ start, end }] } with ISO-8601 instants, e.g. "2030-01-05T10:00:00Z".
 */
export const createCar = (listing) => request('/api/cars', jsonRequest('POST', listing))

/** booking: { start, end } as ISO-8601 instants, e.g. "2030-01-05T10:00:00Z". */
export const createBooking = (carId, booking) =>
  request(`/api/cars/${carId}/bookings`, jsonRequest('POST', booking))

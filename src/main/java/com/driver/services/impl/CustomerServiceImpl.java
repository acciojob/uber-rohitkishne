package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

			List<Driver> drivers = driverRepository2.findAll();

			Comparator<Driver> compareById = new Comparator<Driver>() {
				@Override
				public int compare(Driver d1, Driver d2) {
					return Integer.compare(d1.getDriverId(), d2.getDriverId());
				}
			};

			Collections.sort(drivers, compareById);

			boolean driverAvail = false;
			int driverId = 0;
			for(Driver driver : drivers)
			{
				if(driver.getCab().getAvailable()== true)
				{
					driverId = driver.getDriverId();
					driverAvail = true;
					break;
				}
			}

			if(driverAvail == false) {
				throw new Exception("No cab available!");
			}

			TripBooking booking = new TripBooking();
			booking.setFromLocation(fromLocation);
			booking.setToLocation(toLocation);
			booking.setDistanceInKm(distanceInKm);
			booking.setStatus(TripStatus.CONFIRMED);

			Customer customer =customerRepository2.findById(customerId).get();
			customer.getTripBookingList().add(booking);
			booking.setCustomer(customer);
			customerRepository2.save(customer);

		    Driver driver = driverRepository2.findById(driverId).get();
			driver.getCab().setAvailable(true);
			driver.getTripBookingList().add(booking);
			booking.setDriver(driver);
			driverRepository2.save(driver);

			return booking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking booking = tripBookingRepository2.findById(tripId).get();
		booking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(booking);


	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking booking = tripBookingRepository2.findById(tripId).get();
		booking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(booking);

	}
}

const express = require("express");
const { QuickDB } = require("quick.db");
const app = express();
const db = new QuickDB();
app.use(express.json());

app.post("/book-ticket", async (req, res) => {
  const { name, flightId } = req.body;
  const flight = await db.get(`flights.${flightId}`);
  if (!flight)
    return res.status(404).json({ sucess: false, error: "Flight not found" });

  const ticketId = `TICKET_${Date.now()}`;
  const ticket = { ticketId, name, flightId };
  await db.set(`tickets.${ticketId}`, ticket);
  console.log(ticket);
  res.json({ success: true, ticket });
});

app.get("/search-ticket/:ticketId", async (req, res) => {
  const ticket = await db.get(`tickets.${req.params.ticketId}`);
  if (!ticket)
    return res.status(404).json({ sucess: false, error: "Ticket not found" });
  console.log(ticket);
  res.json({ success: true, ticket });
});

app.get("/search-flights", async (req, res) => {
  const { departure, arrival } = req.query;
  const flights = (await db.get("flights")) || {};
  const filteredFlights = Object.values(flights).filter(
    (flight) => flight.departure === departure && flight.arrival === arrival
  );
  console.log(filteredFlights);
  res.json({ flights: filteredFlights });
});

app.post("/cancel-ticket", async (req, res) => {
  const { ticketId } = req.body;
  const ticket = await db.get(`tickets.${ticketId}`);
  if (!ticket)
    return res.status(404).json({ sucess: false, error: "Ticket not found" });

  await db.delete(`tickets.${ticketId}`);
  res.json({ success: true, message: "Ticket cancelled" });
});

app.post("/add-flight", async (req, res) => {
  const { flightId, departure, arrival } = req.body;
  const flight = { flightId, departure, arrival };
  await db.set(`flights.${flightId}`, flight);
  res.json({ success: true, flight });
});

app.post("/delete-flight", async (req, res) => {
  const { flightId } = req.body;
  const flight = await db.get(`flights.${flightId}`);
  if (!flight)
    return res.status(404).json({ sucess: false, error: "Flight not found" });

  await db.delete(`flights.${flightId}`);
  res.json({ success: true, message: "Flight deleted" });
});

app.listen(3000, () => {
  console.log("Server running on port 3000");
});

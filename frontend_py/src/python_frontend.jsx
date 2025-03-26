import { useEffect, useState } from "react";

export default function App() {
  const [ping, setPing] = useState("");
  const [message, setMessage] = useState("Hello API");
  const [response, setResponse] = useState("");

  useEffect(() => {
    fetch("http://127.0.0.1:8000/ping")
      .then((res) => res.json())
      .then((data) => setPing(data.response));
  }, []);

  const sendEcho = async () => {
    const res = await fetch("http://127.0.0.1:8000/echo", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message })
    });
    const data = await res.json();
    setResponse(data.received);
  };

  return (
    <div className="p-4 text-center">
      <h1 className="text-xl font-bold">FastAPI + React</h1>
      <p>Ping Response: {ping}</p>
      <input 
        className="border p-2 m-2"
        value={message} 
        onChange={(e) => setMessage(e.target.value)}
      />
      <button className="bg-blue-500 text-white p-2" onClick={sendEcho}>Send</button>
      <p>Echo Response: {response}</p>
    </div>
  );
}


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

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
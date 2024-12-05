const { exec } = require("child_process");
const express = require("express");
const bodyParser = require("body-parser");

const app = express();
const PORT = 3000;

app.use(bodyParser.json());
app.use(express.static("frontend")); // Serve static frontend files

app.post("/run", (req, res) => {
    const { command, args } = req.body;

    const javaCommand = `java -cp . moviereview ${command} ${args || ""}`;
    exec(javaCommand, (error, stdout, stderr) => {
        if (error) {
            console.error(`Error: ${error.message}`);
            return res.status(500).send("Server Error");
        }
        if (stderr) {
            console.error(`Stderr: ${stderr}`);
            return res.status(500).send("Java Error");
        }
        res.send(stdout); 
    });
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});

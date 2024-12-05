async function runCommand(command, args) {
    try {
        const response = await fetch("/run", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ command, args }),
        });

        const result = await response.text();
        document.getElementById("output").textContent = result;
    } catch (error) {
        console.error("Error:", error);
        document.getElementById("output").textContent = "An error occurred. Please try again.";
    }
}

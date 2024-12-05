const { spawn } = require("child_process");

// Spawn the Java process
const javaProcess = spawn("java", ["-cp", ".", "MovieReviewCLI"]);

// Handle output from Java
javaProcess.stdout.on("data", (data) => {
    console.log(`Java says: ${data}`);
});

// Handle errors
javaProcess.stderr.on("data", (data) => {
    console.error(`Java error: ${data}`);
});

// Handle process exit
javaProcess.on("close", (code) => {
    console.log(`Java process exited with code ${code}`);
});

// Example: Sending commands to Java
setTimeout(() => {
    javaProcess.stdin.write("display\n");
}, 1000);

setTimeout(() => {
    javaProcess.stdin.write("search\n");
    javaProcess.stdin.write("Inception\n");
}, 2000);

setTimeout(() => {
    javaProcess.stdin.write("exit\n");
}, 5000);

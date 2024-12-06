const { spawn } = require("child_process");

// Spawn the Java process
const javaProcess = spawn("java", ["-cp", ";c:\\Users\\austi\\Downloads\\mysql-connector-j-9.1.0\\mysql-connector-j-9.1.0.jar", "MovieReviewCLI.java"]);

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

setTimeout(() => {
    javaProcess.stdin.write("display\n");
}, 1000);

setTimeout(() => {
    javaProcess.stdin.write("search\n");
}, 2000);

setTimeout(() => {
    javaProcess.stdin.write("exit\n");
}, 5000);


document.getElementById("signup").addEventListener("submit", async (event) => {
    event.preventDefault();

    console.log("clicked from signup...");

    const name = document.querySelector("#name").value;
    const email = document.querySelector("#email").value;
    const address = document.querySelector("#address").value;
    const password = document.querySelector("#password").value;

    console.log(name, email, address, password);

    let credential = { password };

    const reqBody = JSON.stringify({ name, email, address, credential });

    console.log("before call the api..")
    try {
        let response = await fetch("http://localhost:8090/users/signup", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: reqBody
        });
        console.log("after call the api...")

        const data = await response.json();

        console.log("response: ", data)

        alert(data.message);

        window.location.href = "../Login/index.html";

    } catch (error) {
        console.error(error);
    }

});


document.getElementById("login").addEventListener("submit", async (event) => {
    event.preventDefault();

    console.log("clicked...");

    const userId = document.querySelector("#username").value;
    const password = document.querySelector("#password").value;

    console.log(userId, password);

    const reqBody = JSON.stringify({ userId, password });

    let response = await fetch("http://localhost:8090/users/login", {
        method: "POST",
        headers: {
            'Content-Type': 'application/json'
        },
        body: reqBody
    });

    const data = await response.json();

    if (response.ok) {

        console.log("data:", data);

        alert(data.message);

        sessionStorage.setItem('token', JSON.stringify(data.user.token));
        sessionStorage.setItem('id', JSON.stringify(data.user.email));

        window.location.href = "../Home/home.html";

    } else {
        alert(data.message);
    }
});

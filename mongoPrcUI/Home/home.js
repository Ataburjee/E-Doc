window.addEventListener('DOMContentLoaded', async () => {
    const container = document.getElementById('documents');
    const token = JSON.parse(sessionStorage.getItem('token'));
    const userId = JSON.parse(sessionStorage.getItem('id'));

    console.log(token, userId)

    let userDocs = await fetch(`http://localhost:8090/documents?id=${userId}`, {
        method: "GET",
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });

    let response = await userDocs.json();

    console.log(response.documents)

    //For create document
    let div = document.createElement("div");
    div.classList.add("document");

    let content = document.createElement("a");
    content.textContent = "Create Document";

    div.appendChild(content);

    div.addEventListener("click", () => {
        sessionStorage.setItem('document', null);
        window.location.href = "../Document/document.html";
    })

    container.appendChild(div);

    //Retrieve & Append all documents
    response.documents.forEach(doc => {

        console.log("doc: ", doc)

        let div = document.createElement("div");
        div.classList.add("document");

        let title = document.createElement("p");
        let content = document.createElement("p");

        title.textContent = `Title: ${doc.title}`;
        content.textContent = `Content: ${doc.content}`;

        div.appendChild(title);
        div.appendChild(content);

        container.appendChild(div);

        div.addEventListener("click", () => {
            sessionStorage.setItem('document', JSON.stringify(doc));
            window.location.href = "../Document/document.html";
        })
    });
});
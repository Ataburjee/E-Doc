const seletedDocument = sessionStorage.getItem('document');
const token = JSON.parse(sessionStorage.getItem('token'));
const userId = JSON.parse(sessionStorage.getItem('userId'));

let doc = JSON.parse(seletedDocument);

console.log("selected doc: ", doc)

let titleText = document.querySelector("#titleText");

async function docTitleText() {
    console.log("Inside titleText:", titleText.innerText);

    let title = titleText.innerText;

    if (doc && token) {
        let response = await fetch(`http://localhost:8090/documents/${doc._id}`, {
            method: "PUT",
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ title })
        })

        if (response.status == 202) {
            let docRes = await fetch(`http://localhost:8090/documents/${doc._id}?id=${userId}`, {
                method: "GET",
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            })
            if (docRes.ok) {
                let setDoc = await docRes.json();
                if (setDoc) {
                    sessionStorage.setItem('document', JSON.stringify(setDoc));
                }
            }
        }
    }
}

try {

    if (doc) {

        console.log(doc)

        titleText.textContent = doc.titleText;

        function shareDocument() {
            console.log("share button");
        }

    } else {
        titleText.textContent = "New Document";

        function saveDocument() {
            console.log("save button");
        }
    }

} finally {
    console.log("Can't access the resource");
}

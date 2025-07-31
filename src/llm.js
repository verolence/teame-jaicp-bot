import axios from "axios"

const answerMatchesQuestion = (question, answer) => {
    let result = (answer === question);
    return result;
};

// const prompt = "Generate an array of 5 names in Russian in the format [\"name\", \"name\", ...] for a bot persona based on the following json description. The names can include whitespaces.\n\nDESCRIPTION:\n" + JSON.stringify(description, null, 2) + "\n\nARRAY OF 5 NAMES:";

async function cailaRequest(query) {
    
    log(`>>> Query: ${toPrettyString(query)}`)
    
    const account = "just-ai";
    const model = "gemini";
    const url = `https://caila.io/api/mlpgate/account/${account}/model/${model}/predict`;
    const token = $secrets.get("CAILA_TOKEN", "");
    
    const headers = {
        "MLP-API-KEY": token,
        "Content-Type": "application/json"
    };
    
    let body = {
        "model": "gemini-2.0-flash-lite",
        "messages": [
            { 
                "role": "system", 
                "content": "отвечай стихами" 
            },
            {
                "role": "user",
                "content": query
            }
        ],
        "temperature": 1
    };
    
    log(`>>> Body: ${toPrettyString(body)}`);
    
    log(`>>> Url: ${toPrettyString(url)}`);
    
    const res = await axios.post(
        url,
        body,
        {headers: headers}
    );
    let llmRes = res.data.choices[0].message.content;
    return llmRes; 

    // try {
    //     const res = await axios.post(
    //         `https://caila.io/api/mlpgate/account/${account}/model/${model}/predict`,
    //         JSON.stringify(body),
    //         {
    //             headers: headers,
    //             httpsAgent: new https.Agent({ rejectUnauthorized: false })  // ← Игнорировать SSL-ошибки
    //         }
    //     );
    //     let llmRes = res.data.choices[0].message.content;
    //     return llmRes; 
    // } catch (e) {
    //     throw new Error(">>> Error calling Caila API in llmRequest" + JSON.stringify(e));
    // }
}

export default { answerMatchesQuestion, cailaRequest };
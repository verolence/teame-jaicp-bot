const answerMatchesQuestion = (question, answer) => {
    let result = (answer === question);
    return result;
};

// const prompt = "Generate an array of 5 names in Russian in the format [\"name\", \"name\", ...] for a bot persona based on the following json description. The names can include whitespaces.\n\nDESCRIPTION:\n" + JSON.stringify(description, null, 2) + "\n\nARRAY OF 5 NAMES:";

async function cailaRequest(query) {
    
    const account = "just-ai";
    const model = "gemini";
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

    try {
        const res = await axios.post(
            `https://caila.io/api/mlpgate/account/${account}/model/${model}/predict`,
            body,
            {headers: headers}
        );
        res = res.data.choices[0].message.content;
        return res; 
    } catch (e) {
        throw new Error(">>> Error calling Caila API in llmRequest" + JSON.stringify(e));
    }
}

export default { answerMatchesQuestion, cailaRequest };
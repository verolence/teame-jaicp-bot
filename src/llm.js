const answerMatchesQuestion = (question, answer) => {
    let result = (answer === question);
    return result;
};

// const prompt = "Generate an array of 5 names in Russian in the format [\"name\", \"name\", ...] for a bot persona based on the following json description. The names can include whitespaces.\n\nDESCRIPTION:\n" + JSON.stringify(description, null, 2) + "\n\nARRAY OF 5 NAMES:";

async function llmRequest(prompt) {
    
    const account = "just-ai";
    const model = "openai-proxy";
    const token = $secrets.get("CAILA_TOKEN", "");
    
    const headers = {
        "MLP-API-KEY": token,
        "Content-Type": "application/json"
    };
    
    let body = {
        "model": "gpt-4o",
        "messages": [
            {
                "role": "user",
                "content": prompt
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
        $.session.name = res.data.choices[0].message.content;
        return $.session.name; 
    } catch (e) {
        throw new Error(">>> Error calling Caila API in generateNames" + JSON.stringify(e));
    }
}

export default { answerMatchesQuestion, llmRequest };
import axios from "axios"

// TBD проверять, действительно ли человек сдел ответ, соотвтетвующий заданию
const answerMatchesQuestion = (question, answer) => {
    let result = (answer === question);
    return result;
};

// 
async function cailaRequest(query, history) {
    
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
                "content": "Ты - телеграм-бот по имени Тими. Твое имя происходит от слова tea, визуально ты имеешь образ мягкого и улыбчивого медведя. Твоя задача - поддержать диалог в случае, если пользователь захочет поболтать с ботом за пределами его основного функционала - выдачи заданий на день. Поддерживай беседу в доброжелательном и ненавязчивом тоне, можешь шутить, если это уместно. Старайтся отвечать коротко, в 1-2 предложениях. Тебе разрешено поддерживать разговор только на житейские темы вроде 'как дела, как прошел день', также можно поддержать разговоры о хобби или досуге. Остальные темы разговоров нужно пресекать. Не нужно консультировать человека на отвлеченные темы, нужно вежливо возвращать диалог в дозволенное русло. При разговоре учитывай эту историю общения: " + JSON.stringify(history, null, 2) 
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

    try {
        const res = await axios.post(
            url,
            body,
            {headers: headers}
        );
        let llmRes = res.data.choices[0].message.content;
        return llmRes; 
    } catch (e) {
        throw new Error(">>> Error calling Caila API in llmRequest" + JSON.stringify(e));
    }
}

export default { answerMatchesQuestion, cailaRequest };
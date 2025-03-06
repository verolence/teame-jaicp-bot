require: modules.js
    type = scriptEs6
    name = modules

require: patterns.sc
  module = sys.zb-common

init:
    bind("onAnyError", function() {
        $reactions.answer("Извините, произошла техническая ошибка. Пожалуйста, напишите в чат позже.");
    });

theme: /

    state: Start
        q!: $regex</start>
        script:
            $jsapi.startSession();
        go!: ./Welcome
        
        state: Welcome
            a: Привет! Меня зовут Тими. Я хочу стать твоим компаньоном, подбрасывать идеи, как сделать обычные будние дни немного ярче и счастливее.
            a: А ты знаешь, что новый опыт и изучение всего неизвестного оказывают мощное влияние на настроение и мозг?
            buttons: 
                "Расскажи больше!"
            q: * || toState = "/Start/ScienceFact"

        state: ScienceFact
            a: Психологи считают, что новые впечатления и опыт вызывают в мозге человека повышенную активность, аналогичную той, что связана с ощущением счастья. 
            a: Как раз с этим я и могу помочь! Давай покажу, что я умею, и проведу тебя за руку через первое задание? Это займет меньше минуты!
            buttons: 
                "Получить задание!"
            q: * || toState = "/Start/Onboarding"
            
        state: Onboarding
            a: Твое задание на сегодня: практика благодарности. Напиши мне 2-3 вещи, за которые ты можешь поблагодарить этот день. Это может быть что-то очень значимое, а, может, и совсем простое. Благодарность за солнечное утро, или грячую воду в кране.
            a: Когда выпишешь все благодарности, нажми кнопку "готово".
            buttons: 
                "Готово"
                
            state: CathGratitude
                q: *
                buttons: 
                    "Готово"
                go: ..
                
            state: Finished
                q: готово *
                a: Супер! Это ведь было не сложно? Но день стал уже чуточку приятнее, когда удалось заметить в нем приятные вещи.
        
    # state: WaitFile
    #     q: $agree || fromState="/SumUpFile"
    #     a: Пришлите мне файл, и я покажу вам свою магию!

    # state: AcceptFile
    #     event!: fileEvent
    #     scriptEs6:
    #         const fileObject = $request.data.eventData[0];
    #         const extension = _.last(fileObject.url.split("."));
    #         const allowedFiletypes = ["pdf", "png", "jpeg", "jpg", "gif", "bmp", "tiff", "tif"];
    #         $temp.isAllowedFileType = allowedFiletypes.includes(extension);
    #         $temp.fileUrl = fileObject.url;
    #     if: !$temp.isAllowedFileType
    #         a: Ой, я умею работать только c форматами PDF, PNG, JPEG, GIF, TIFF и BMP. Пожалуйста, загрузите другой файл.
    #     else:
    #         scriptEs6:
    #             try {
    #                 // Очищение директории для хранения временных файлов.
    #                 // Подробнее о $storage: https://help.cloud.just-ai.com/jaicp/JS_API/es6-file-storage
    #                 await $storage.clear();
    #                 // getTempDir возвращает путь до директории для хранения временных файлов.
    #                 const dir = await $storage.getTempDir();
    #                 const filePath = modules.path.join(dir, `${$request.channelUserId}-${modules.path.basename($temp.fileUrl)}`);
    #                 // Получение объекта и его запись во временное хранилище.
    #                 $session.filePath = await new Promise((resolve, reject) => {
    #                     modules.axios
    #                     // Получение элемента по ссылке.
    #                     .get($temp.fileUrl, { responseType: "stream" })
    #                     .then(({ data }) => {
    #                         const writer = modules.fs.createWriteStream(filePath);
    #                         data.pipe(writer);
    #                         writer.on("finish", () => resolve(filePath));
    #                         writer.on("error", reject);
    #                     })
    #                     .catch(reject);
    #                 });
    #             } catch (err) {
    #                 $reactions.answer("Прошу прощения, произошла ошибка. Попробуйте еще раз позже.");
    #                 $reactions.transition("/GoodBye");
    #             };
    #         go!: /ReadFile

    # state: ReadFile
    #     scriptEs6:
    #         try {
    #             // Библиотека Ocr Space позволяет выполнять не более 10 запросов за 10 минут.
    #             // С API-ключом - 25000 запросов в месяц. Ключ можно записать в секрет "OCR_SPACE_API_KEY".
    #             // Информация про ключ для Osr Space: https://ocr.space/OCRAPI#PostParameters
    #             // Подробнее о секретах: https://help.cloud.just-ai.com/jaicp/script_development/secrets
    #             const response = await modules.ocrSpace($session.filePath, { language: "rus" });
    #             if (response.ErrorMessage && response.ErrorMessage[0] === "File failed validation. File size exceeds the maximum permissible file size limit of 1024 KB") {
    #                 $reactions.transition("/Handlers/FileTooBig");
    #             }
    #             if (response.IsErrorOnProcessing) throw new Error("Error while processing file with Ocr-Space");
    #             // Функция ocrSpace возвращает данные о полученном объекте.
    #             // В частности - поле ParsedText, в котором хранится распознанный в файле текст.
    #             $session.fileContent = response.ParsedResults?.[0]?.ParsedText;
    #             if (!$session.fileContent) throw new Error("Ocr-Space didn't return the content");
    #             $temp.isRecognizeSuccessful = true;
    #         } catch(err) {
    #             $temp.isRecognizeSuccessful = false;
    #         }
    #     if: $temp.isRecognizeSuccessful
    #         go!: /SumUpFile
    #     else:
    #         a: Прошу прощения, произошла ошибка. Попробуйте еще раз позже.
    #         go!: /GoodBye
            
    # state: SumUpFile
    #     a: Занимаюсь обработкой. Это может занять какое-то время.
    #     scriptEs6:
    #         const content = `Could you please provide a summary of the given text, including all key points and supporting details?
    #         The summary should be comprehensive and accurately reflect the main message and arguments presented in the original text, while also being concise and easy to understand. To ensure accuracy, please read the text carefully and pay attention to any nuances or complexities in the language. Additionally, the summary should avoid any personal biases or interpretations and remain objective and factual throughout.
    #         Write your answer on russian.
    #         Text:
    #         ` + $session.fileContent;
    #         // Функция $gpt.createChatCompletion возвращает результат обращения к сервису gpt. В данном случае - с целью саммаризации.
    #         // Подробнее о $gpt.createChatCompletion: https://help.cloud.just-ai.com/jaicp/JS_API/built_in_services/gpt/createChatCompletion
    #         $gpt.createChatCompletion([{ "role": "assistant", "content": content }])
    #             .then(({ choices }) => {
    #                 // С помощью sendRepliesToClient можно отправить сразу несколько сообщений за раз.
    #                 // Вначале выводится сообщение о начале обработки, затем приходит ответ от gpt, затем - последовательно выводятся три ответа ниже.
    #                 // Подробнее о conversationApi: https://help.cloud.just-ai.com/jaicp/JS_API/built_in_services/conversationApi
    #                 $conversationApi.sendRepliesToClient([
    #                 { type: "text", text: "Подготовил краткое изложение!" },
    #                 { type: "text", text: choices[0].message.content },
    #                 { type: "text", text: "Хотите получить изложение еще какого-нибудь файла?"}
    #             ]);
    #         })
    #         //Если при саммаризации произошла ошибка, программа переключается на действия в catch.
    #         .catch((err) => {
    #             $conversationApi.sendTextToClient("Прошу прощения, произошла ошибка. Попробуйте еще раз позже.");
    #         });

    #     state: CatchAll || noContext=true
    #         event: noMatch
    #         a: К сожалению, не смог понять, что вы имеете в виду. Подскажите, я могу помочь с изложением еще одного файла?

    state: GoodBye
        q: $disagree || fromState="/Start"
        q!: $bye
        a: Жаль, что нам приходится прощаться! Если что, заглядывай снова, я буду ждать тебя.
        script:
            $jsapi.stopSession();

theme: /GeneralStates

    state: NoMatch || noContext=true
        event!: noMatch
        a: Прошу прощения, но я не могу уловить суть. Мой функционал ограничен обработкой файлов в формате PDF, PNG, JPEG, GIF, TIFF и BMP.

theme: /Handlers
    
    state: FileTooBig
        # Подробнее о fileTooBigEvent: https://help.cloud.just-ai.com/jaicp/script_development/events/fileTooBigEvent
        event!: fileTooBigEvent
        a: Прошу прощения, но этот файл слишком большой. Я могу обработать файлы не более чем 1МБ.

    state: LimitHandler
        # Подробнее о системных событиях: https://help.cloud.just-ai.com/jaicp/script_development/events#request-limit-events
        event!: lengthLimit
        event!: timeLimit
        event!: nluSystemLimit
        a: Извините, я не могу обработать сообщение — оно слишком большое. Пожалуйста, перефразируйте его покороче.
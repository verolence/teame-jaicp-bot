require: modules.js
    type = scriptEs6
    name = modules
    
require: llm.js
    type = scriptEs6
    name = llm

require: patterns.sc
  module = sys.zb-common
  
require: answers.yaml
    var = answers

init:
    bind("onAnyError", function() {
        $reactions.answer("Извини, произошла техническая ошибка. Пожалуйста, напиши мне позже, либо сообщи об ошибке моей создательнице @vero_lence");
    });

theme: /

    state: Start
        q!: $regex</start>
        script:
            $jsapi.startSession();
        go!: /Onboarding/Welcome
        
    state: GoodBye
        q: $disagree || fromState="/Start"
        q!: $bye
        a: {{ answers["Bye"] }}
        script:
            $jsapi.stopSession();
            
    state: History
        q!: $regex</history>
        a: {{ $session.currentTask }} - {{ $session.taskAnswer }}
        
    state: NewTask
        q!: $regex</newtask>
        random:
            a: Когда будешь возвращаться домой, пройди непривычным для себя маршрутом. Сфотографируй для меня любой интересный объект, который раньше не замечал.
            a: Сделай себе массаж головы, а также аккуратно разомни уши. Ты сразу почувствуешь, как будет благодарна нервная система. Опиши свои ощущения в паре слов.
            a: Выбери для себя "девиз дня". Что бы тебе сегодня хотелось: продуктивно провести день, или наоборот, отстать немного от себя и позволить отдохнуть?
        a: Ты можешь сразу написать свой ответ, либо взять задание сейчас, а затем использовать команду /finishtask для его завершения
        buttons: 
            "Взять задание!"
            "Другое задание"
        
    state: FinishTask
        q!: Сдать задание
        q!: $regex</finishtask>
        a: Чтобы завершить задание, загрузи или напиши свой ответ в наш чат
        
        state: Finished   
            q: *
            event: fileEvent
            a: Супер! Надеюсь, твой день стал чуточку радостнее. Завтра предложу тебе новое развлечение, а пока предлагаю поболтать.
            go: /

theme: /Onboarding
    
    state: Welcome
        a: {{ answers["WelcomeHello"] }}
        a: {{ answers["WelcomeQuestion"] }}
        buttons: 
            "Расскажи больше!"
        q: * || toState = "/Onboarding/ScienceFact"

    state: ScienceFact
        a: Психологи считают, что новые впечатления и опыт вызывают в мозге человека повышенную активность, аналогичную той, что связана с ощущением счастья. 
        a: Как раз с этим я и могу помочь! Давай покажу, что я умею, и проведу тебя за руку через первое задание? Это займет меньше минуты!
        buttons: 
            "Взять задание!"
        q: * || toState = "/Onboarding/FirstTask"
        
    state: FirstTask
        q: Сдать задание
        q: $regex</finishtask>
        a: Твое задание на сегодня: практика благодарности. Напиши мне 2-3 вещи, за которые ты можешь поблагодарить этот день. Это может быть что-то очень значимое, а, может, и совсем простое. Благодарность за солнечное утро, или грячую воду в кране.
        script: 
            $session.currentTask = "Практика благодарности"
            
        state: CatchGrattitude
            event: noMatch
            scriptEs6:
                if (testMode() || llm.answerMatchesQuestion()) {
                    $session.taskAnswer = $request.query;
                    $reactions.answer("Супер! Это ведь было не сложно? Но день стал уже чуточку лучше, когда удалось заметить в нем светлые вещи.");
                    $reactions.answer("По команде /history ты сможешь найти свои ответы на задания, а с командой /newtask получить новое предложение от меня.");
                    $reactions.transition("/");
                } else {
                    reactions.tr$ansition("/Freestyle/Convo");
                }

theme: /Freestyle
    
    state: Convo
        event!: noMatch
        scriptEs6:
            if (testMode()) {
                $reactions.answer("Рад бы поболтать с тобой, но функционал свободной беседы пока находится в стадии разработки!");
            } else {
                //let llmAnswer = "кря";
                let llmAnswer = llm.cailaRequest($request.query);
                $reactions.answer(llmAnswer);
            }

theme: /Handlers
    
    state: FileTooBig || noContex = true
        event!: fileTooBigEvent
        a: Прошу прощения, но этот файл слишком большой. Я могу обработать файлы не более чем 1МБ.

    state: LimitHandler || noContex = true
        event!: lengthLimit
        event!: timeLimit
        event!: nluSystemLimit
        a: Извини, я не могу обработать твое сообщение — оно слишком большое. Пожалуйста, перефразируй свою мысль покороче.
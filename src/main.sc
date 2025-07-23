require: modules.js
    type = scriptEs6
    name = modules
    
require: llm.js
    type = scriptEs6
    name = llm

require: patterns.sc
  module = sys.zb-common

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
        a: Жаль, что нам приходится прощаться! Если что, заглядывай снова, я буду ждать тебя.
        script:
            $jsapi.stopSession();

theme: /Onboarding
    
    state: Welcome
        a: Привет! Меня зовут Тими. Я хочу стать твоим компаньоном, подбрасывать идеи, как сделать обычные будние дни немного ярче и счастливее.
        a: А ты знаешь, что новый опыт и изучение всего неизвестного оказывают мощное влияние на настроение и мозг?
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
            q: *
            if: llm.answerMatchesQuestion()
                script: $session.taskAnswer = $request.query;
                a: Супер! Это ведь было не сложно? Но день стал уже чуточку лучше, когда удалось заметить в нем светлые вещи.
                a: По команде /history ты сможешь найти свои ответы на задания, а с командой /newtask получить новое предложение от меня. 
            else: 
                go!: /Freestyle/Convo

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
        
theme: /Freestyle
    
    state: Convo
        event!: noMatch
        a: Рад бы поболтать с тобой, но функционал свободной беседы пока находится в стадии разработки!

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
        a: Извини, я не могу обработать твое сообщение — оно слишком большое. Пожалуйста, перефразируй свою мысль покороче.
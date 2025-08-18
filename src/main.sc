require: modules.js
    type = scriptEs6
    name = modules
    
require: llm.js
    type = scriptEs6
    name = llm

require: patterns.sc
  module = sys.zb-common
  
require: dicts/answers.yaml
    var = answers
    
# https://help.cloud.just-ai.com/jaicp/common/bot_structure/csv
require: dicts/tasks.csv
    name = Tasks
    var = $Tasks

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
        intent!: NewTask
        q!: Новое задание
        q!: $regex</newtask>
        random:
            a: Когда будешь возвращаться домой, пройди непривычным для себя маршрутом. Сфотографируй для меня любой интересный объект, который раньше не замечал.
            a: Сделай себе массаж головы, а также аккуратно разомни уши. Ты сразу почувствуешь, как будет благодарна нервная система. Опиши свои ощущения в паре слов.
            a: Выбери для себя "девиз дня". Что бы тебе сегодня хотелось: продуктивно провести день, или наоборот, отстать немного от себя и позволить отдохнуть?
        a: Ты можешь сразу написать свой ответ, либо взять задание сейчас, а затем использовать команду /finishtask для его завершения
        buttons: 
            "Взять задание!"
            # TBD "Другое задание"
        
        # TBD state: ChangeTask
        
    state: FinishTask
        intent!: FinishTask
        q!: Сдать задание
        q!: $regex</finishtask>
        a: Чтобы завершить задание, загрузи или напиши свой ответ в наш чат
        
        state: Finished   
            q: *
            event: fileEvent
            a: Супер! Надеюсь, твой день стал чуточку радостнее. Хочешь, предложу еще одну идею, или просто поболтаем?
            buttons: 
                "Новое задание"
                "Просто поболтать"
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
        script: 
            $session.currentTaskNumber = 1;
            $session.currentTask = $Tasks[$session.currentTaskNumber];
            log(toPrettyString($session.currentTask));
        a: Твое задание на сегодня: {{ $session.currentTask.alternateNames[0]}}. {{ $session.currentTask.value.task}}
        q: * || toState = "/Onboarding/FirstTask/CatchGrattitude"
        event: fileEvent || toState = "/Onboarding/FirstTask/CatchGrattitude"

        state: CatchGrattitude
            event: noMatch
            script:
                $session.taskAnswer = $request.query;
                $reactions.answer(answers["OnboardingCatchAnswer"]);
                $reactions.answer(answers["OnboardingDescrComments"]);
            buttons: 
                "Новое задание"
                "Просто поболтать"
            go: /

theme: /Freestyle
    
    state: Convo
        event!: noMatch
        script: 
            $temp.context = $jsapi.chatHistoryInLlmFormat();
            # TBD if ($temp.context.length > 10) llm.shortenHistory($temp.context);
        scriptEs6:
            if (testMode()) {
                $reactions.answer("Рад бы поболтать с тобой, но функционал свободной беседы пока находится в стадии разработки!");
            } else {
                let llmAnswer = await llm.cailaRequest($request.query, $temp.context);
                $reactions.answer(llmAnswer);
                
                if ($session.currentTask) $reactions.buttons({ text: "Новое задание", transition: "/NewTask" });
                else $reactions.buttons({ text: "Сдать задание", transition: "/FinishTask" });
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
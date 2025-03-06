// Модуль node:fs позволяет взаимодействовать с файловой системой по образцу стандартных функций POSIX.
// Подробнее о модуле node:fs: https://nodejsdev.ru/api/fs/
import fs from "node:fs";

// Модуль node:path предоставляет утилиты для работы с путями к файлам и каталогам.
// Подробнее о модуле node:path: https://nodejsdev.ru/api/path/
import path from "node:path";

// Axios - это HTTP-клиент, основанный на Promise для node.js и браузера.
// Подробнее о модуле Axios: https://axios-http.com/ru/docs/intro
import axios from "axios";

// Модуль "ocr-space-api-wrapper" используется для распознавания текста на изображениях и в PDF-файлах.
// Подробнее о модуле "ocr-space-api-wrapper" (документация на английском): https://www.npmjs.com/package/ocr-space-api-wrapper
import { ocrSpace } from "ocr-space-api-wrapper";

export default { fs, path, axios, ocrSpace };
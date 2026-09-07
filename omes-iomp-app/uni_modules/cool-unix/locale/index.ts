import en from "./en.json";
import zhcn from "./zh-cn.json";
import zhtw from "./zh-tw.json";
import es from "./es.json";
import ja from "./ja.json";
import fr from "./fr.json";
import ko from "./ko.json";
import { forInObject, isNull } from "../cool";

export const t = (name: string) => {
	// 根据自己项目的多语言方案配置
	// 也可以使用 cool-unix 完整项目中的多语言方案
	return name;
};

export const $t = (name: string, data: any) => {
	let text = t(name);

	// 替换参数
	if (!isNull(data)) {
		forInObject(data, (value, key) => {
			if (typeof value === "number") {
				value = value.toString();
			}

			text = text.replaceAll(`{${key}}`, value as string);
		});
	}

	return text;
};

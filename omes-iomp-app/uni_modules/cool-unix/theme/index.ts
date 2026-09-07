import { ref } from "vue";
import { ctx, isNull } from "../cool";

export const isDark = ref(false);

export const setTheme = (theme: "light" | "dark") => {
	isDark.value = theme == "dark";
};

export const getColor = (name: string) => {
	if (isNull(ctx.color)) {
		return "";
	}

	return ctx.color[name] as string;
};

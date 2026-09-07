import { reactive } from "vue";

type Config = {
	fontSize: number | null;
	zIndex: number;
	startDate: string;
	endDate: string;
	isCustomTabBar: boolean;
	backTop: boolean;
};

export const config = reactive<Config>({
	fontSize: null,
	zIndex: 600,
	startDate: "2000-01-01T00:00:00",
	endDate: "2050-12-31T23:59:59",
	isCustomTabBar: false,
	backTop: true
});

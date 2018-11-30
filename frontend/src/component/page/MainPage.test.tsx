import {shallow} from "enzyme";
import React from 'react';
import {BrowserRouter} from "react-router-dom";
import 'reflect-metadata';
import Client from "../../model/Client";
import SearchItem from "../../model/SearchItem";
import MainPage, {IMainPageProps} from "./MainPage";

describe("component", () => {
    let props: IMainPageProps;

    beforeEach(() => {
        props = {onStatusChange: ()=>undefined, client: new Client("admin", "admin"), onSearchItemsUpdate: (items: SearchItem[]) => undefined};
    });

    describe("rendering", () => {
        it('renders without crashing', () => {
            shallow(<BrowserRouter><MainPage {...props}/></BrowserRouter>)
        });
    });
});
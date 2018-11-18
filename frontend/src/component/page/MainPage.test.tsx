import {shallow} from "enzyme";
import React from 'react';
import {BrowserRouter} from "react-router-dom";
import 'reflect-metadata';
import Client from "../../model/Client";
import MainPage, {IMainPageProps} from "./MainPage";

describe("component", () => {
    let props: IMainPageProps;

    beforeEach(() => {
        props = {client: new Client()};
    });

    describe("rendering", () => {
        it('renders without crashing', () => {
            shallow(<BrowserRouter><MainPage {...props}/></BrowserRouter>)
        });
    });
});